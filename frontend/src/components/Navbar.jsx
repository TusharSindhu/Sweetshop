import { Link, useNavigate } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem('token'); // Check if token exists
  const username = localStorage.getItem('username'); // Optional: Show who is logged in

  const handleLogout = () => {
    // 1. Clear Storage
    localStorage.clear();
    // 2. Redirect to Login
    navigate('/login');
  };

  return (
    <nav className="bg-brand-500 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo / Title */}
          <Link to="/" className="text-2xl font-bold tracking-wide flex items-center gap-2">
            🍬 Mithai Wala
          </Link>

          {/* Navigation Links */}
          <div className="flex items-center space-x-4">
            {isLoggedIn ? (
              <>
                <span className="text-brand-100 text-sm hidden sm:block">
                  Hello, {username}
                </span>
                <button 
                  onClick={handleLogout} 
                  className="bg-white text-brand-600 px-4 py-2 rounded-md font-bold hover:bg-gray-100 transition shadow-sm"
                >
                  Logout
                </button>
              </>
            ) : (
              <Link to="/login" className="bg-white text-brand-600 px-4 py-2 rounded-md font-bold hover:bg-gray-100 transition shadow-sm">
                Login
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}