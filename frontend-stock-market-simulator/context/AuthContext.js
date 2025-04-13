// context/AuthContext.jsx
"use client";

import { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {jwtDecode} from "jwt-decode";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

// In AuthContext.jsx
useEffect(() => {
  const checkLoginStatus = () => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decoded = jwtDecode(token);
        const isExpired = decoded.exp * 1000 < Date.now();
        
        setIsLoggedIn(!isExpired);
        if (!isExpired) {
          setUser(decoded.sub); // or whatever user info you need
        } else {
          handleLogout();
        }
      } catch (e) {
        console.error("Token decoding failed:", e);
        handleLogout();
      }
    } else {
      setIsLoggedIn(false);
      setUser(null);
    }
    setLoading(false);
  };

  checkLoginStatus();

  // Optional: Add event listener for storage changes
  const handleStorageChange = () => {
    checkLoginStatus();
  };

  window.addEventListener('storage', handleStorageChange);
  return () => window.removeEventListener('storage', handleStorageChange);
}, []);

  
  

  const handleLogin = async (credentials) => {
    try {
      // Call login API
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.data.token);
        setIsLoggedIn(true);
        setUser(data.data.user);
        return { success: true };
      } else {
        const error = await response.json();
        return { success: false, message: error.message || "Login failed" };
      }
    } catch (error) {
      console.error("Login error:", error);
      return { success: false, message: "Network error occurred" };
    }
  };

  const handleRegister = async (credentials) => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
      });
  
      if (response.status === 201) {

        router.push("/login");
  
        // return { success: true };
      } else {
        const error = await response.json();
        return { success: false, message: error.message || "Registration failed" };
      }
    } catch (error) {
      console.error("Register error:", error);
      return { success: false, message: "Network error occurred" };
    }
  };
  

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    setUser(null);
    router.push("/");
  };

  const checkAccess = (requiredAuth = true) => {
    if (loading) return false;
    
    if (requiredAuth && !isLoggedIn) {
      router.push("/login");
      return false;
    }
    
    return true;
  };

  const value = {
    isLoggedIn,
    user,
    loading,
    login: handleLogin,
    logout: handleLogout,
    register: handleRegister,
    checkAccess,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}