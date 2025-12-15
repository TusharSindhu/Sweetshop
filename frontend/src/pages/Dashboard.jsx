import { useEffect, useState } from 'react';
import axios from 'axios';
import SweetCard from '../components/SweetCard';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const [sweets, setSweets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // We define the fetch function internally to avoid infinite loops
  useEffect(() => {
    const fetchSweets = async () => {
      const token = localStorage.getItem('token');
      
      if (!token) {
        setError('Please login to view sweets');
        setLoading(false);
        return;
      }

      try {
        const response = await axios.get('http://localhost:8080/api/sweets', {
          headers: { Authorization: `Bearer ${token}` }
        });
        setSweets(response.data);
      } catch (err) {
        console.error(err);
        setError('Failed to load sweets. You might need to login again.');
      } finally {
        setLoading(false);
      }
    };

    fetchSweets();
  }, []); // Empty array means "run once when page loads"

  // We need a separate simplified version for the Purchase button to refresh data
  const refreshSweets = async () => {
    const token = localStorage.getItem('token');
    try {
      const response = await axios.get('http://localhost:8080/api/sweets', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setSweets(response.data);
    } catch (err) { 
      console.error(err);
    }
  };

  const handlePurchase = async (id) => {
    const token = localStorage.getItem('token');
    try {
      await axios.post(`http://localhost:8080/api/sweets/${id}/purchase`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      // After purchase, refresh the list to show new stock
      refreshSweets();
      alert("Purchase Successful! 🍬");
    } catch (err) {
      console.error(err);
      alert("Failed to purchase. Try again.");
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
      <h2 className="text-3xl font-bold text-brand-900 mb-8">Freshly Made Sweets</h2>
      
      {sweets.length === 0 ? (
        <p className="text-gray-500">No sweets available yet. Ask the Admin to add some!</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {sweets.map(sweet => (
            <SweetCard key={sweet.id} sweet={sweet} onPurchase={handlePurchase} />
          ))}
        </div>
      )}
    </div>
  );
}