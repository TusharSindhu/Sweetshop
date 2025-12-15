import { Link } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="bg-brand-500 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo / Title */}
          <Link to="/" className="text-2xl font-bold tracking-wide">
            🍬 Mithai Wala
          </Link>

          {/* Navigation Links */}
          <div className="flex space-x-4">
            <Link to="/login" className="hover:bg-brand-600 px-3 py-2 rounded-md transition">
              Login
            </Link>
            <Link to="/cart" className="bg-white text-brand-600 px-4 py-2 rounded-md font-bold hover:bg-gray-100 transition">
              Cart (0)
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}