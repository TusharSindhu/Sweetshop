import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';
import SweetCard from '../components/SweetCard';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const [sweets, setSweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  
  // Search State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [maxPrice, setMaxPrice] = useState('');

  // Admin Form State
  const [showForm, setShowForm] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [formData, setFormData] = useState({ id: null, name: '', category: '', price: '', quantity: '' });

  // 1. UPDATED: Fetch Data from Backend with Filters
  const fetchSweets = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setError('Please login to view sweets');
      setLoading(false);
      return;
    }

    try {
      // Create URL parameters
      const params = new URLSearchParams();
      if (searchQuery) params.append('name', searchQuery);
      if (selectedCategory) params.append('category', selectedCategory);
      if (maxPrice) params.append('maxPrice', maxPrice);

      // It always hits the /search endpoint now, which handles empty params gracefully
      const url = `http://localhost:8080/api/sweets/search?${params.toString()}`;
      
      const response = await axios.get(url, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSweets(response.data);
      setError('');
    } catch (err) {
      console.log(err);
      setError('Failed to load sweets. You might need to login again.');
    } finally {
      setLoading(false);
    }
  }, [searchQuery, selectedCategory, maxPrice]); // Re-run if these change

  // Initial Load & Admin Check
  useEffect(() => {
    fetchSweets();
    const role = localStorage.getItem('role');
    setIsAdmin(role === 'ROLE_ADMIN');
  }, [fetchSweets]);

  // Handle "Go" button for Name search
  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchSweets();
  };

  // --- Admin Logic (Same as before) ---
  const handlePurchase = async (id) => {
    const token = localStorage.getItem('token');
    try {
      await axios.post(`http://localhost:8080/api/sweets/${id}/purchase`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchSweets();
      alert("Purchased! 🍬");
    } catch (err) {
      console.log(err);
      alert("Purchase Failed");
    }
  };

  const handleDelete = async (id) => {
    if(!window.confirm("Delete this sweet?")) return;
    const token = localStorage.getItem('token');
    try {
      await axios.delete(`http://localhost:8080/api/sweets/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchSweets();
    } catch (err) {
      console.log(err);
      alert("Delete failed");
    }
  };

  const handleRestock = async (id) => {
    const qty = window.prompt("Enter quantity to add to stock:");
    if (!qty || isNaN(qty) || qty <= 0) return;

    const token = localStorage.getItem('token');
    try {
      await axios.post(`http://localhost:8080/api/sweets/${id}/restock`, { quantity: parseInt(qty) }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchSweets();
      alert("Stock updated!");
    } catch (err) {
      console.log(err);
      alert("Restock failed");
    }
  };

  const openAddForm = () => {
    setFormData({ id: null, name: '', category: '', price: '', quantity: '' });
    setIsEditMode(false);
    setShowForm(true);
  };

  const openEditForm = (sweet) => {
    setFormData({ ...sweet });
    setIsEditMode(true);
    setShowForm(true);
    window.scrollTo(0, 0);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    try {
      if (isEditMode) {
        await axios.put(`http://localhost:8080/api/sweets/${formData.id}`, formData, {
          headers: { Authorization: `Bearer ${token}` }
        });
        alert("Sweet Updated!");
      } else {
        await axios.post('http://localhost:8080/api/sweets', formData, {
          headers: { Authorization: `Bearer ${token}` }
        });
        alert("Sweet Added!");
      }
      setShowForm(false);
      fetchSweets();
    } catch (err) {
      console.log(err);
      alert("Operation failed");
    }
  };

  if (loading) return <div className="text-center mt-10">Loading tasty sweets...</div>;
  if (error) return <div className="text-center mt-10 text-red-600">{error}</div>;

  return (
    <div>
      {/* Top Controls */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4 bg-white p-4 rounded shadow-sm border border-brand-100">
        <h2 className="text-3xl font-bold text-brand-900">Dashboard</h2>
        
        <div className="flex flex-col md:flex-row gap-2 w-full md:w-auto">
          {/* Search Inputs */}
          <form onSubmit={handleSearchSubmit} className="flex gap-2">
            <input 
              type="text" placeholder="Search Name..." 
              className="px-3 py-2 border rounded text-sm w-full"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="bg-brand-500 text-white px-3 py-2 rounded text-sm font-bold">Go</button>
          </form>

          {/* Filters (Trigger fetchSweets automatically via useEffect) */}
          <input 
            type="text" placeholder="Filter Category" 
            className="px-3 py-2 border rounded text-sm"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          />
          <input 
            type="number" placeholder="Max Price" 
            className="px-3 py-2 border rounded text-sm w-24"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />

          {/* Admin Toggle */}
          {isAdmin && (
            <button 
              onClick={showForm ? () => setShowForm(false) : openAddForm}
              className={`px-4 py-2 rounded font-bold text-white whitespace-nowrap ${showForm ? 'bg-gray-500' : 'bg-green-600 hover:bg-green-700'}`}
            >
              {showForm ? 'Close Form' : '+ Add Sweet'}
            </button>
          )}
        </div>
      </div>

      {/* Admin Form */}
      {showForm && (
        <div className="bg-brand-50 p-6 rounded shadow-md mb-8 border border-brand-200">
          <h3 className="font-bold mb-4 text-brand-900 text-xl">{isEditMode ? 'Edit Sweet' : 'Add New Sweet'}</h3>
          <form onSubmit={handleFormSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input placeholder="Name" className="border p-2 rounded" required
              value={formData.name}
              onChange={e => setFormData({...formData, name: e.target.value})} />
            <input placeholder="Category" className="border p-2 rounded" required
              value={formData.category}
              onChange={e => setFormData({...formData, category: e.target.value})} />
            <input type="number" placeholder="Price" className="border p-2 rounded" required
              value={formData.price}
              onChange={e => setFormData({...formData, price: e.target.value})} />
            <input type="number" placeholder="Quantity" className="border p-2 rounded" required
              value={formData.quantity}
              onChange={e => setFormData({...formData, quantity: e.target.value})} />
            <button type="submit" className="md:col-span-2 bg-brand-600 text-white py-2 rounded hover:bg-brand-700 font-bold">
              {isEditMode ? 'Update Sweet' : 'Save Sweet'}
            </button>
          </form>
        </div>
      )}

      {/* Results Grid */}
      {sweets.length === 0 ? (
        <div className="text-center py-10">
          <p className="text-gray-500 text-lg">No sweets found.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {sweets.map(sweet => (
            <SweetCard 
              key={sweet.id} 
              sweet={sweet} 
              isAdmin={isAdmin}
              onPurchase={handlePurchase}
              onEdit={openEditForm}
              onRestock={handleRestock}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
}