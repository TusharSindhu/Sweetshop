import { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';

export default function Register() {
  const [formData, setFormData] = useState({ username: '', password: '', email: '' });
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/auth/register', formData);
      alert("Registration Successful! Please Login.");
      navigate('/login');
    } catch (err) {
        console.error(err);
        alert("Registration failed. Try a different username.");
    }
  };

  return (
    <div className="flex justify-center items-center mt-10">
      <div className="bg-white p-8 rounded-lg shadow-lg w-96 border-t-4 border-brand-500">
        <h2 className="text-2xl font-bold mb-6 text-center text-brand-900">Register</h2>
        <form onSubmit={handleRegister} className="space-y-4">
          <input 
            type="text" placeholder="Username" required
            className="w-full px-3 py-2 border rounded"
            onChange={e => setFormData({...formData, username: e.target.value})}
          />
          <input 
            type="email" placeholder="Email" required
            className="w-full px-3 py-2 border rounded"
            onChange={e => setFormData({...formData, email: e.target.value})}
          />
          <input 
            type="password" placeholder="Password" required
            className="w-full px-3 py-2 border rounded"
            onChange={e => setFormData({...formData, password: e.target.value})}
          />
          <button type="submit" className="w-full bg-brand-600 text-white py-2 rounded hover:bg-brand-700">
            Sign Up
          </button>
        </form>
        <p className="mt-4 text-center text-sm">
          Already have an account? <Link to="/login" className="text-brand-600 font-bold">Login</Link>
        </p>
      </div>
    </div>
  );
}