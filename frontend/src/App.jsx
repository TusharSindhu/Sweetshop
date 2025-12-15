import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-brand-50">
        {/* The Navbar will appear on every page */}
        <Navbar />

        {/* This container adds padding and centers the content */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            {/* The Dashboard is the "Home" page */}
            <Route path="/" element={<Dashboard />} />
            
            {/* The Login page is separate */}
            {<Route path="/login" element={<Login />} />}
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;