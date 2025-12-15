import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // Connect to your Spring Boot Backend
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        username,
        password
      });

      // Save the JWT token to local storage
      const token = response.data.jwtToken;
      localStorage.setItem('token', token);
      localStorage.setItem('username', response.data.username);
      localStorage.setItem('role', response.data.role);

      // Redirect to home/dashboard
      navigate('/');
      
    } catch (err) {
      setError('Invalid username or password');
    }
  };

  return (
    <div className="flex justify-center items-center mt-10">
      <div className="bg-white p-8 rounded-lg shadow-lg w-96 border-t-4 border-brand-500">
        <h2 className="text-2xl font-bold mb-6 text-center text-brand-900">Login</h2>
        
        {error && <div className="bg-red-100 text-red-700 p-2 rounded mb-4 text-sm">{error}</div>}
        
        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Username</label>
            <input 
              type="text" 
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-brand-500 focus:border-brand-500"
              required 
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Password</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-brand-500 focus:border-brand-500"
              required 
            />
          </div>

          <button 
            type="submit" 
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-brand-600 hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500"
          >
            Sign In
          </button>
        </form>
        <p className="mt-4 text-center text-sm">
        Don't have an account? <Link to="/register" className="text-brand-600 font-bold">Register here</Link>
        </p>
      </div>
    </div>
  );
}