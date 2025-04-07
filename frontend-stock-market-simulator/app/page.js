// app/page.jsx or pages/index.jsx (depending on your Next.js setup)
"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

// Import components
import Navbar from "@/components/Navbar";
import SearchBar from "@/components/SearchBar";
import MarketSummary from "@/components/MarketSummary";
import UserDashboard from "@/components/UserDashboard";
import StockTable from "@/components/StockTable";
import Footer from "@/components/Footer";

const HomePage = () => {
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [trendingStocks, setTrendingStocks] = useState([]);
  const [recommendedStocks, setRecommendedStocks] = useState([]);
  const [marketSummary, setMarketSummary] = useState({});
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem("token");
    setIsLoggedIn(!!token);

    // Fetch trending stocks and market data
    fetchTrendingStocks();
    fetchMarketSummary();
    
    // Fetch recommended stocks if logged in
    if (token) {
      fetchRecommendedStocks();
    }
  }, []);

  const fetchTrendingStocks = async () => {
    try {
      setIsLoading(true);
      // This would be replaced with your actual API endpoint
      const response = await fetch("http://localhost:8080/api/stocks/trending");
      const data = await response.json();
      setTrendingStocks(data);
    } catch (error) {
      console.error("Error fetching trending stocks:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchRecommendedStocks = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/api/stocks/recommended", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await response.json();
      setRecommendedStocks(data);
    } catch (error) {
      console.error("Error fetching recommended stocks:", error);
    }
  };

  const fetchMarketSummary = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/market/summary");
      const data = await response.json();
      setMarketSummary(data);
    } catch (error) {
      console.error("Error fetching market summary:", error);
    }
  };

  const handleStockSelect = (stockSymbol) => {
    router.push(`/stock/${stockSymbol}`);
  };

  const handleAddToWatchlist = async (stockSymbol) => {
    if (!isLoggedIn) {
      router.push("/login");
      return;
    }
    
    try {
      const token = localStorage.getItem("token");
      await fetch("http://localhost:8080/api/watchlist/add", {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}` 
        },
        body: JSON.stringify({ symbol: stockSymbol })
      });
      alert(`${stockSymbol} added to watchlist!`);
    } catch (error) {
      console.error("Error adding to watchlist:", error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    router.push("/");
  };

  // Mock data for demonstration
  const mockTrendingStocks = [
    { symbol: "AAPL", name: "Apple Inc.", price: 239.51, change: 2.34, changePercent: 0.99 },
    { symbol: "MSFT", name: "Microsoft Corporation", price: 406.87, change: -1.24, changePercent: -0.30 },
    { symbol: "AMZN", name: "Amazon.com Inc.", price: 182.21, change: 3.45, changePercent: 1.93 },
    { symbol: "TSLA", name: "Tesla, Inc.", price: 215.35, change: 7.89, changePercent: 3.80 },
    { symbol: "NVDA", name: "NVIDIA Corporation", price: 950.02, change: -12.34, changePercent: -1.28 }
  ];

  const mockRecommendedStocks = [
    { symbol: "GOOGL", name: "Alphabet Inc.", price: 172.02, change: 1.45, changePercent: 0.85 },
    { symbol: "IBM", name: "International Business Machines", price: 247.83, change: -0.45, changePercent: -0.18 },
    { symbol: "NFLX", name: "Netflix, Inc.", price: 624.18, change: 12.43, changePercent: 2.03 }
  ];

  const mockMarketSummary = {
    dowJones: { value: 39933.14, change: 135.76, changePercent: 0.34 },
    nasdaq: { value: 16385.60, change: -51.93, changePercent: -0.32 },
    sp500: { value: 5232.21, change: 10.87, changePercent: 0.21 }
  };

  // Use mock data if API calls are not implemented
  useEffect(() => {
    if (trendingStocks.length === 0 && !isLoading) {
      setTrendingStocks(mockTrendingStocks);
    }
    if (recommendedStocks.length === 0 && isLoggedIn) {
      setRecommendedStocks(mockRecommendedStocks);
    }
    if (Object.keys(marketSummary).length === 0) {
      setMarketSummary(mockMarketSummary);
    }
  }, [isLoading, isLoggedIn]);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation */}
      <Navbar isLoggedIn={isLoggedIn} handleLogout={handleLogout} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Hero Section */}
        {!isLoggedIn && (
          <div className="bg-blue-600 rounded-xl text-white p-8 mb-8">
            <div className="max-w-3xl">
              <h1 className="text-3xl font-bold mb-3">Start Your Investment Journey Today</h1>
              <p className="text-lg mb-6">Practice trading with virtual money and build your portfolio risk-free.</p>
              <div className="flex space-x-4">
                <Button 
                  onClick={() => router.push("/register")}
                  className="bg-white text-blue-600 hover:bg-gray-100"
                >
                  Get Started
                </Button>
                <Button 
                  onClick={() => router.push("/learn")}
                  variant="outline" 
                  className="border-white text-white hover:bg-blue-700"
                >
                  Learn More
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* Search Bar */}
        <SearchBar isLoggedIn={isLoggedIn} router={router} />

        {/* Market Summary */}
        <MarketSummary marketSummary={marketSummary} />

        {/* Dashboard Summary (for logged-in users) */}
        {isLoggedIn && <UserDashboard router={router} />}

        {/* Stock Listings */}
        <Tabs defaultValue="trending" className="mb-8">
          <TabsList>
            <TabsTrigger value="trending">Trending Stocks</TabsTrigger>
            {isLoggedIn && (
              <TabsTrigger value="recommended">Recommended For You</TabsTrigger>
            )}
          </TabsList>
          <TabsContent value="trending">
            <StockTable 
              stocks={trendingStocks}
              title="Trending Stocks"
              description="Popular stocks with significant market activity"
              handleStockSelect={handleStockSelect}
              handleAddToWatchlist={handleAddToWatchlist}
            />
          </TabsContent>
          
          {isLoggedIn && (
            <TabsContent value="recommended">
              <StockTable 
                stocks={recommendedStocks}
                title="Recommended For You"
                description="Stocks tailored to your investment preferences"
                handleStockSelect={handleStockSelect}
                handleAddToWatchlist={handleAddToWatchlist}
              />
            </TabsContent>
          )}
        </Tabs>

        {/* Call to Action (for guests) */}
        {!isLoggedIn && (
          <div className="rounded-lg bg-gray-100 p-6 flex flex-col md:flex-row justify-between items-center">
            <div>
              <h3 className="text-xl font-bold mb-2">Ready to start investing?</h3>
              <p className="text-gray-600 mb-4 md:mb-0">Create an account to track stocks, build a portfolio, and practice investing.</p>
            </div>
            <div className="flex gap-4">
              <Button 
                onClick={() => router.push("/login")}
                variant="outline"
              >
                Login
              </Button>
              <Button 
                onClick={() => router.push("/register")}
              >
                Sign Up
              </Button>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default HomePage;