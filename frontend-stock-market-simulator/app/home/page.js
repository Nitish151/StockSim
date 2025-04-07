"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { 
  Card, 
  CardContent, 
  CardHeader, 
  CardTitle, 
  CardDescription 
} from "@/components/ui/card";
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from "@/components/ui/table";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger
} from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search, TrendingUp, TrendingDown, Star, Menu, X, User, LogOut, BarChart3 } from "lucide-react";
import Navbar from "@/components/Navbar";
const HomePage = () => {
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [trendingStocks, setTrendingStocks] = useState([]);
  const [recommendedStocks, setRecommendedStocks] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
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

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/stocks/search?stockName=${searchTerm}`);
      const data = await response.json();
      console.log(data.body);
       setSearchResults(data.body);
      console.log(searchResults);
    } catch (error) {
      console.error("Error searching stocks:", error);
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
    setIsMenuOpen(false);
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
                  <Button onClick={handleLogout} variant="outline" className="ml-4">Logout</Button>
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
                    onClick={handleLogout}
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
        <div className="mb-8">
          <div className="flex gap-2">
            <div className="relative flex-grow">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
              <Input 
                type="text" 
                placeholder="Search for stocks by name or symbol..." 
                className="pl-10 w-full"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <Button onClick={handleSearch}>Search</Button>
          </div>
          
          {/* Search Results */}
          {searchResults.length > 0 && (
            <Card className="mt-4">
              <CardHeader>
                <CardTitle>Search Results</CardTitle>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Symbol</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Exchange</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {searchResults.map((stock) => (
                      <TableRow key={stock.symbol}>
                        <TableCell className="font-medium">{stock.symbol}</TableCell>
                        <TableCell>{stock.name}</TableCell>
                        <TableCell>{stock.exchDisp}</TableCell>
                        <TableCell>{stock.typeDisp}</TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button 
                              size="sm" 
                              variant="outline"
                              onClick={() => handleStockSelect(stock.symbol)}
                            >
                              View
                            </Button>
                            <Button 
                              size="sm" 
                              variant="outline"
                              onClick={() => handleAddToWatchlist(stock.symbol)}
                            >
                              <Star size={16} />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Market Summary */}
        <div className="mb-8">
          <h2 className="text-xl font-bold mb-4">Market Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Object.keys(marketSummary).length > 0 && (
              <>
                <Card>
                  <CardContent className="pt-6">
                    <h3 className="text-lg font-medium mb-1">Dow Jones</h3>
                    <p className="text-2xl font-bold">{marketSummary.dowJones.value.toLocaleString()}</p>
                    <p className={marketSummary.dowJones.change >= 0 ? "text-green-600" : "text-red-600"}>
                      {marketSummary.dowJones.change >= 0 ? "+" : ""}{marketSummary.dowJones.change.toFixed(2)} ({marketSummary.dowJones.changePercent.toFixed(2)}%)
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="pt-6">
                    <h3 className="text-lg font-medium mb-1">NASDAQ</h3>
                    <p className="text-2xl font-bold">{marketSummary.nasdaq.value.toLocaleString()}</p>
                    <p className={marketSummary.nasdaq.change >= 0 ? "text-green-600" : "text-red-600"}>
                      {marketSummary.nasdaq.change >= 0 ? "+" : ""}{marketSummary.nasdaq.change.toFixed(2)} ({marketSummary.nasdaq.changePercent.toFixed(2)}%)
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent className="pt-6">
                    <h3 className="text-lg font-medium mb-1">S&P 500</h3>
                    <p className="text-2xl font-bold">{marketSummary.sp500.value.toLocaleString()}</p>
                    <p className={marketSummary.sp500.change >= 0 ? "text-green-600" : "text-red-600"}>
                      {marketSummary.sp500.change >= 0 ? "+" : ""}{marketSummary.sp500.change.toFixed(2)} ({marketSummary.sp500.changePercent.toFixed(2)}%)
                    </p>
                  </CardContent>
                </Card>
              </>
            )}
          </div>
        </div>

        {/* Dashboard Summary (for logged-in users) */}
        {isLoggedIn && (
          <div className="mb-8">
            <h2 className="text-xl font-bold mb-4">Your Dashboard</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Card>
                <CardHeader>
                  <CardTitle>Portfolio Value</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-2xl font-bold">$13,248.82</p>
                  <p className="text-green-600">+$124.56 (0.95%)</p>
                  <Button 
                    variant="link" 
                    className="p-0 h-auto mt-2"
                    onClick={() => router.push("/portfolio")}
                  >
                    View Portfolio →
                  </Button>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle>Watchlist</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-lg">5 stocks</p>
                  <p className="text-sm text-gray-500 mb-2">Last updated 5 min ago</p>
                  <Button 
                    variant="link" 
                    className="p-0 h-auto"
                    onClick={() => router.push("/watchlist")}
                  >
                    View Watchlist →
                  </Button>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle>Cash Balance</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-2xl font-bold">$4,751.18</p>
                  <Button 
                    variant="outline" 
                    className="mt-2"
                    onClick={() => router.push("/deposit")}
                  >
                    Add Funds
                  </Button>
                </CardContent>
              </Card>
            </div>
            <div className="mt-4">
              <Button 
                className="flex items-center"
                onClick={() => router.push("/dashboard")}
              >
                <BarChart3 size={18} className="mr-2" /> Full Dashboard
              </Button>
            </div>
          </div>
        )}

        {/* Stock Listings */}
        <Tabs defaultValue="trending" className="mb-8">
          <TabsList>
            <TabsTrigger value="trending">Trending Stocks</TabsTrigger>
            {isLoggedIn && (
              <TabsTrigger value="recommended">Recommended For You</TabsTrigger>
            )}
          </TabsList>
          <TabsContent value="trending">
            <Card>
              <CardHeader>
                <CardTitle>Trending Stocks</CardTitle>
                <CardDescription>Popular stocks with significant market activity</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Symbol</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Price</TableHead>
                      <TableHead>Change</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {trendingStocks.map((stock) => (
                      <TableRow key={stock.symbol}>
                        <TableCell className="font-medium">{stock.symbol}</TableCell>
                        <TableCell>{stock.name}</TableCell>
                        <TableCell>${stock.price.toFixed(2)}</TableCell>
                        <TableCell>
                          <div className="flex items-center">
                            {stock.change >= 0 ? (
                              <TrendingUp size={16} className="text-green-600 mr-1" />
                            ) : (
                              <TrendingDown size={16} className="text-red-600 mr-1" />
                            )}
                            <span className={stock.change >= 0 ? "text-green-600" : "text-red-600"}>
                              {stock.change >= 0 ? "+" : ""}{stock.change.toFixed(2)} ({stock.changePercent.toFixed(2)}%)
                            </span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button 
                              size="sm" 
                              variant="outline"
                              onClick={() => handleStockSelect(stock.symbol)}
                            >
                              View
                            </Button>
                            <Button 
                              size="sm" 
                              variant="outline"
                              onClick={() => handleAddToWatchlist(stock.symbol)}
                            >
                              <Star size={16} />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>
          
          {isLoggedIn && (
            <TabsContent value="recommended">
              <Card>
                <CardHeader>
                  <CardTitle>Recommended For You</CardTitle>
                  <CardDescription>Stocks tailored to your investment preferences</CardDescription>
                </CardHeader>
                <CardContent>
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Symbol</TableHead>
                        <TableHead>Name</TableHead>
                        <TableHead>Price</TableHead>
                        <TableHead>Change</TableHead>
                        <TableHead></TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {recommendedStocks.map((stock) => (
                        <TableRow key={stock.symbol}>
                          <TableCell className="font-medium">{stock.symbol}</TableCell>
                          <TableCell>{stock.name}</TableCell>
                          <TableCell>${stock.price.toFixed(2)}</TableCell>
                          <TableCell>
                            <div className="flex items-center">
                              {stock.change >= 0 ? (
                                <TrendingUp size={16} className="text-green-600 mr-1" />
                              ) : (
                                <TrendingDown size={16} className="text-red-600 mr-1" />
                              )}
                              <span className={stock.change >= 0 ? "text-green-600" : "text-red-600"}>
                                {stock.change >= 0 ? "+" : ""}{stock.change.toFixed(2)} ({stock.changePercent.toFixed(2)}%)
                              </span>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex gap-2">
                              <Button 
                                size="sm" 
                                variant="outline"
                                onClick={() => handleStockSelect(stock.symbol)}
                              >
                                View
                              </Button>
                              <Button 
                                size="sm" 
                                variant="outline"
                                onClick={() => handleAddToWatchlist(stock.symbol)}
                              >
                                <Star size={16} />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
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

      <footer className="bg-gray-100 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
              <h3 className="text-lg font-bold mb-4">StockSimulator</h3>
              <p className="text-gray-600">Learn to invest without the risk. Practice trading with virtual money.</p>
            </div>
            <div>
              <h3 className="text-lg font-bold mb-4">Features</h3>
              <ul className="space-y-2">
                <li><a href="/portfolio" className="text-gray-600 hover:text-gray-900">Portfolio Management</a></li>
                <li><a href="/watchlist" className="text-gray-600 hover:text-gray-900">Watchlists</a></li>
                <li><a href="/news" className="text-gray-600 hover:text-gray-900">Market News</a></li>
                <li><a href="/analytics" className="text-gray-600 hover:text-gray-900">Analytics</a></li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-bold mb-4">Resources</h3>
              <ul className="space-y-2">
                <li><a href="/learn" className="text-gray-600 hover:text-gray-900">Learning Center</a></li>
                <li><a href="/faq" className="text-gray-600 hover:text-gray-900">FAQs</a></li>
                <li><a href="/blog" className="text-gray-600 hover:text-gray-900">Blog</a></li>
                <li><a href="/support" className="text-gray-600 hover:text-gray-900">Support</a></li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-bold mb-4">Company</h3>
              <ul className="space-y-2">
                <li><a href="/about" className="text-gray-600 hover:text-gray-900">About Us</a></li>
                <li><a href="/contact" className="text-gray-600 hover:text-gray-900">Contact</a></li>
                <li><a href="/privacy" className="text-gray-600 hover:text-gray-900">Privacy Policy</a></li>
                <li><a href="/terms" className="text-gray-600 hover:text-gray-900">Terms of Service</a></li>
              </ul>
            </div>
          </div>
          <div className="mt-8 pt-8 border-t border-gray-200">
            <p className="text-gray-500 text-center">© 2025 StockSimulator. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default HomePage;