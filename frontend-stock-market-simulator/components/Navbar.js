// components/Navbar.jsx
"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { LogOut, Menu, X } from "lucide-react";
import { useAuth } from "@/context/AuthContext";

const Navbar = () => {
  const router = useRouter();
  const { isLoggedIn, logout } = useAuth();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  
  return (
    <header className="bg-white shadow">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <h1 className="text-xl font-bold text-gray-900">StockSimulator</h1>
          </div>
          
          {/* Desktop Navigation */}
          <div className="hidden md:flex md:items-center md:space-x-6">
            <a href="/" className="text-gray-900 hover:text-blue-600">Home</a>
            <a href="/market" className="text-gray-600 hover:text-blue-600">Market</a>
            <a href="/news" className="text-gray-600 hover:text-blue-600">News</a>
            {isLoggedIn ? (
              <>
                <a href="/portfolio" className="text-gray-600 hover:text-blue-600">Portfolio</a>
                <a href="/watchlist" className="text-gray-600 hover:text-blue-600">Watchlist</a>
                <a href="/dashboard" className="text-gray-600 hover:text-blue-600">Dashboard</a>
                <Button onClick={logout} variant="outline" className="ml-4">Logout</Button>
              </>
            ) : (
              <>
                <a href="/login" className="text-gray-600 hover:text-blue-600">Login</a>
                <a href="/register" className="bg-blue-600 text-white px-4 py-2 rounded-md font-medium">Sign Up</a>
              </>
            )}
          </div>
          
          {/* Mobile menu button */}
          <div className="md:hidden">
            <button 
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="p-2 rounded-md text-gray-600 hover:text-gray-900 focus:outline-none"
            >
              {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>
      </div>
      
      {/* Mobile menu */}
      {isMenuOpen && (
        <div className="md:hidden">
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            <a href="/" className="block px-3 py-2 text-base font-medium text-gray-900">Home</a>
            <a href="/market" className="block px-3 py-2 text-base font-medium text-gray-600">Market</a>
            <a href="/news" className="block px-3 py-2 text-base font-medium text-gray-600">News</a>
            {isLoggedIn ? (
              <>
                <a href="/portfolio" className="block px-3 py-2 text-base font-medium text-gray-600">Portfolio</a>
                <a href="/watchlist" className="block px-3 py-2 text-base font-medium text-gray-600">Watchlist</a>
                <a href="/dashboard" className="block px-3 py-2 text-base font-medium text-gray-600">Dashboard</a>
                <button 
                  onClick={logout}
                  className="flex items-center w-full px-3 py-2 text-base font-medium text-red-600"
                >
                  <LogOut size={18} className="mr-2" /> Logout
                </button>
              </>
            ) : (
              <>
                <a href="/login" className="block px-3 py-2 text-base font-medium text-gray-600">Login</a>
                <a href="/register" className="block px-3 py-2 text-base font-medium text-blue-600">Sign Up</a>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Navbar;