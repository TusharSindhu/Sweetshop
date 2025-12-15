import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';
import SweetCard from '../components/SweetCard';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const [sweets, setSweets] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  
  // Admin Form State
  const [showAddForm, setShowAddForm] = useState(false);
  const [newSweet, setNewSweet] = useState({ name: '', category: '', price: '', quantity: '' });

  // FIX: Use useCallback to create a stable function accessible everywhere
  const fetchSweets = useCallback(async (query = '') => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      setError('Please login to view sweets');
      setLoading(false);
      return;
    }

    try {
      // Decide URL based on whether we are searching or listing all
      const url = query 
        ? `http://localhost:8080/api/sweets/search?name=${query}` 
        : 'http://localhost:8080/api/sweets';
      
      const response = await axios.get(url, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSweets(response.data);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Failed to load sweets. You might need to login again.');
    } finally {
      setLoading(false);
    }
  }, []); // Empty dependency array means this function never changes

  // Initial Load & Admin Check
  useEffect(() => {
    fetchSweets();
    const role = localStorage.getItem('role');
    setIsAdmin(role === 'ROLE_ADMIN');
  }, [fetchSweets]);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchSweets(searchQuery);
  };

  const handlePurchase = async (id) => {
    const token = localStorage.getItem('token');
    try {
      await axios.post(`http://localhost:8080/api/sweets/${id}/purchase`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchSweets(searchQuery); // Refresh list
      alert("Purchase Successful! 🍬");
    } catch (err) {
      console.error(err);
      alert("Failed to purchase. Try again.");
    }
  };

  const handleDelete = async (id) => {
    if(!window.confirm("Are you sure you want to delete this sweet?")) return;
    const token = localStorage.getItem('token');
    try {
      await axios.delete(`http://localhost:8080/api/sweets/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchSweets(searchQuery);
    } catch (err) {
      console.error(err);
      alert("Only Admins can delete sweets!");
    }
  };

  const handleAddSweet = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    try {
      await axios.post('http://localhost:8080/api/sweets', newSweet, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setShowAddForm(false);
      setNewSweet({ name: '', category: '', price: '', quantity: '' });
      fetchSweets(); // Refresh list
      alert("Sweet Added!");
    } catch (err) {
      console.error(err);
      alert("Failed to add sweet");
    }
  };

  if (loading) return <div className="text-center mt-10">Loading tasty sweets...</div>;

  if (error) return (
    <div className="text-center mt-10">
      <p className="text-red-600 mb-4">{error}</p>
      <Link to="/login" className="text-brand-600 underline">Go to Login</Link>
    </div>
  );

  return (
    <div>
      <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
        <h2 className="text-3xl font-bold text-brand-900">Dashboard</h2>
        
        <div className="flex gap-2">
          {/* Search Bar */}
          <form onSubmit={handleSearch} className="flex space-x-2">
            <input 
              type="text" placeholder="Search sweets..." 
              className="px-3 py-2 border rounded border-brand-300 focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="bg-brand-500 text-white px-4 py-2 rounded hover:bg-brand-600 font-medium">
              Search
            </button>
          </form>

          {/* Admin Add Button */}
          {isAdmin && (
            <button 
              onClick={() => setShowAddForm(!showAddForm)}
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 font-bold"
            >
              {showAddForm ? 'Close' : '+ Add'}
            </button>
          )}
        </div>
      </div>

      {/* Admin Add Form */}
      {showAddForm && (
        <div className="bg-white p-6 rounded shadow-md mb-8 border border-brand-200">
          <h3 className="font-bold mb-4 text-brand-900">Add New Sweet</h3>
          <form onSubmit={handleAddSweet} className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input placeholder="Name" className="border p-2 rounded" required
              value={newSweet.name}
              onChange={e => setNewSweet({...newSweet, name: e.target.value})} />
            <input placeholder="Category" className="border p-2 rounded" required
              value={newSweet.category}
              onChange={e => setNewSweet({...newSweet, category: e.target.value})} />
            <input type="number" placeholder="Price" className="border p-2 rounded" required
              value={newSweet.price}
              onChange={e => setNewSweet({...newSweet, price: e.target.value})} />
            <input type="number" placeholder="Quantity" className="border p-2 rounded" required
              value={newSweet.quantity}
              onChange={e => setNewSweet({...newSweet, quantity: e.target.value})} />
            <button type="submit" className="md:col-span-2 bg-brand-600 text-white py-2 rounded hover:bg-brand-700 font-bold">
              Save Sweet
            </button>
          </form>
        </div>
      )}

      {/* Sweet Grid */}
      {sweets.length === 0 ? (
        <div className="text-center py-10">
          <p className="text-gray-500 text-lg">No sweets found. {isAdmin ? "Add some!" : "Come back later!"}</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {sweets.map(sweet => (
            <div key={sweet.id} className="relative group">
              <SweetCard sweet={sweet} onPurchase={handlePurchase} />
              
              {/* Admin Delete Button (Only visible to Admin) */}
              {isAdmin && (
                <button 
                  onClick={() => handleDelete(sweet.id)}
                  className="absolute top-2 right-2 bg-red-600 text-white text-xs px-2 py-1 rounded shadow opacity-90 hover:opacity-100 hover:scale-105 transition"
                >
                  Delete
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}