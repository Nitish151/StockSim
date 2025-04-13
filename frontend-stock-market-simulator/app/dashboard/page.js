"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { ArrowUpRight, ArrowDownRight, DollarSign, Star, TrendingUp, Activity, Calendar, CircleDollarSign } from "lucide-react";
import Navbar from "@/components/Navbar";
import { useAuth } from "@/context/AuthContext";

const Dashboard = () => {
  const router = useRouter();
  const [portfolioData, setPortfolioData] = useState({});
  const [recentTrades, setRecentTrades] = useState([]);
  const [watchlistStocks, setWatchlistStocks] = useState([]);
  const [portfolioPerformance, setPortfolioPerformance] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const {isLoggedIn, loading} = useAuth();

  useEffect(() => {
    // Check if user is logged in
    if (!loading) {
      // Only redirect if definitely not logged in
      if (!isLoggedIn) {
        router.push("/login");
        return;
      }
    }
    
    // Fetch dashboard data
    fetchDashboardData();
  }, [isLoggedIn, router]);

  const fetchDashboardData = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem("token");
      
      // Fetch portfolio summary
      const portfolioResponse = await fetch("http://localhost:8080/api/portfolio/summary", {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log("Portfolio Response:", portfolioResponse);
      const portfolioData = await portfolioResponse.json();
      setPortfolioData(portfolioData);
      
      // Fetch recent trades
      //TODO
      const tradesResponse = await fetch("http://localhost:8080/api/trades/recent", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const tradesData = await tradesResponse.json();
      setRecentTrades(tradesData);
      
      // Fetch watchlist
      const watchlistResponse = await fetch("http://localhost:8080/api/tracking/tracked", {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log("Watchlist Response:", watchlistResponse);
      const watchlistData = await watchlistResponse.json();
      setWatchlistStocks(watchlistData);
      
      // Fetch portfolio performance
      //TODO
      const performanceResponse = await fetch("http://localhost:8080/api/portfolio/performance", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const performanceData = await performanceResponse.json();
      setPortfolioPerformance(performanceData);
      
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
    } finally {
      setIsLoading(false);
    }
  };

  // Mock data for demonstration
  const mockPortfolioData = {
    totalValue: 13248.82,
    invested: 12000.00,
    cashBalance: 4751.18,
    totalReturn: 1248.82,
    totalReturnPercent: 10.41,
    dailyChange: 124.56,
    dailyChangePercent: 0.95
  };

  const mockRecentTrades = [
    { id: 1, type: "BUY", symbol: "AAPL", quantity: 2, price: 239.51, total: 479.02, date: "2025-03-20" },
    { id: 2, type: "SELL", symbol: "MSFT", quantity: 1, price: 406.87, total: 406.87, date: "2025-03-19" },
    { id: 3, type: "BUY", symbol: "GOOGL", quantity: 3, price: 172.02, total: 516.06, date: "2025-03-18" }
  ];

  const mockWatchlistStocks = [
    { symbol: "NVDA", name: "NVIDIA Corporation", price: 950.02, change: -12.34, changePercent: -1.28 },
    { symbol: "TSLA", name: "Tesla, Inc.", price: 215.35, change: 7.89, changePercent: 3.80 },
    { symbol: "AMZN", name: "Amazon.com Inc.", price: 182.21, change: 3.45, changePercent: 1.93 }
  ];

  const mockPortfolioPerformance = [
    { date: "2025-03-17", value: 12800 },
    { date: "2025-03-18", value: 12950 },
    { date: "2025-03-19", value: 13020 },
    { date: "2025-03-20", value: 13100 },
    { date: "2025-03-21", value: 13248.82 }
  ];

  const mockPortfolioAllocation = [
    { name: "AAPL", value: 4790 },
    { name: "GOOGL", value: 3098 },
    { name: "IBM", value: 2230 },
    { name: "MSFT", value: 1620 },
    { name: "Other", value: 1510 }
  ];

  // Use mock data if API calls are not implemented
  useEffect(() => {
    if (Object.keys(portfolioData).length === 0 && !isLoading) {
      setPortfolioData(mockPortfolioData);
    }
    if (recentTrades.length === 0 && !isLoading) {
      setRecentTrades(mockRecentTrades);
    }
    if (watchlistStocks.length === 0 && !isLoading) {
      setWatchlistStocks(mockWatchlistStocks);
    }
    if (portfolioPerformance.length === 0 && !isLoading) {
      setPortfolioPerformance(mockPortfolioPerformance);
    }
  }, [isLoading]);

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  if (isLoading) {
    return <div className="flex h-screen items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold mb-8">Dashboard</h1>
        
        {/* Portfolio Overview */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm text-gray-500">Total Portfolio Value</p>
                  <p className="text-2xl font-bold">${portfolioData.totalValue?.toFixed(2)}</p>
                </div>
                <div className="p-2 bg-blue-100 rounded-full">
                  <DollarSign className="text-blue-600" size={20} />
                </div>
              </div>
              <div className="mt-2 flex items-center">
                {portfolioData.dailyChange >= 0 ? (
                  <ArrowUpRight className="text-green-600 mr-1" size={16} />
                ) : (
                  <ArrowDownRight className="text-red-600 mr-1" size={16} />
                )}
                <span className={portfolioData.dailyChange >= 0 ? "text-green-600" : "text-red-600"}>
                  {portfolioData.dailyChange >= 0 ? "+" : ""}${portfolioData.dailyChange?.toFixed(2)} ({portfolioData.dailyChangePercent?.toFixed(2)}%) Today
                </span>
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm text-gray-500">Total Return</p>
                  <p className="text-2xl font-bold">${portfolioData.totalReturn?.toFixed(2)}</p>
                </div>
                <div className="p-2 bg-green-100 rounded-full">
                  <TrendingUp className="text-green-600" size={20} />
                </div>
              </div>
              <div className="mt-2">
                <span className={portfolioData.totalReturn >= 0 ? "text-green-600" : "text-red-600"}>
                  {portfolioData.totalReturnPercent >= 0 ? "+" : ""}{portfolioData.totalReturnPercent?.toFixed(2)}%
                </span> from investment
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm text-gray-500">Invested Amount</p>
                  <p className="text-2xl font-bold">${portfolioData.invested?.toFixed(2)}</p>
                </div>
                <div className="p-2 bg-purple-100 rounded-full">
                  <Activity className="text-purple-600" size={20} />
                </div>
              </div>
              <div className="mt-2">
                <span className="text-gray-600">
                  Across {mockPortfolioAllocation.length} stocks
                </span>
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm text-gray-500">Cash Balance</p>
                  <p className="text-2xl font-bold">${portfolioData.cashBalance?.toFixed(2)}</p>
                </div>
                <div className="p-2 bg-yellow-100 rounded-full">
                  <CircleDollarSign className="text-yellow-600" size={20} />
                </div>
              </div>
              <div className="mt-2">
                <Button 
                  variant="outline" 
                  className="text-xs h-8"
                  onClick={() => router.push("/deposit")}
                >
                  Add Funds
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
        
        {/* Charts and Tables */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Performance Chart */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Portfolio Performance</CardTitle>
              <CardDescription>Value over the past 5 days</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px]">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart
                    data={portfolioPerformance}
                    margin={{ top: 5, right: 20, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis 
                      dataKey="date" 
                      tickFormatter={(date) => {
                        const d = new Date(date);
                        return `${d.getMonth() + 1}/${d.getDate()}`;
                      }}
                    />
                    <YAxis 
                      domain={['dataMin - 200', 'dataMax + 200']}
                      tickFormatter={(value) => `$${value}`}
                    />
                    <Tooltip 
                      formatter={(value) => [`$${value.toFixed(2)}`, 'Portfolio Value']}
                      labelFormatter={(date) => {
                        const d = new Date(date);
                        return `${d.toLocaleDateString()}`;
                      }}
                    />
                    <Line 
                      type="monotone" 
                      dataKey="value" 
                      stroke="#3b82f6" 
                      strokeWidth={2}
                      dot={{ r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
          
          {/* Portfolio Allocation */}
          <Card>
            <CardHeader>
              <CardTitle>Portfolio Allocation</CardTitle>
              <CardDescription>Distribution by stock</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex justify-center">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={mockPortfolioAllocation}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {mockPortfolioAllocation.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip 
                      formatter={(value) => [`$${value.toFixed(2)}`, 'Value']}
                    />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        </div>
        
        {/* Recent Trades and Watchlist */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Recent Trades */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center">
                <Calendar className="mr-2" size={18} />
                Recent Trades
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Type</TableHead>
                    <TableHead>Symbol</TableHead>
                    <TableHead>Quantity</TableHead>
                    <TableHead>Price</TableHead>
                    <TableHead>Total</TableHead>
                    <TableHead>Date</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {recentTrades.map((trade) => (
                    <TableRow key={trade.id}>
                      <TableCell>
                        <span className={`px-2 py-1 rounded text-xs ${trade.type === 'BUY' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                          {trade.type}
                        </span>
                      </TableCell>
                      <TableCell className="font-medium">{trade.symbol}</TableCell>
                      <TableCell>{trade.quantity}</TableCell>
                      <TableCell>${trade.price.toFixed(2)}</TableCell>
                      <TableCell>${trade.total.toFixed(2)}</TableCell>
                      <TableCell>{new Date(trade.date).toLocaleDateString()}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <div className="mt-4">
                <Button 
                  variant="outline" 
                  className="w-full"
                  onClick={() => router.push("/trades")}
                >
                  View All Trades
                </Button>
              </div>
            </CardContent>
          </Card>
          
          {/* Watchlist */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center">
                <Star className="mr-2" size={18} />
                Watchlist
              </CardTitle>
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
                  {watchlistStocks.map((stock) => (
                    <TableRow key={stock.symbol}>
                      <TableCell className="font-medium">{stock.symbol}</TableCell>
                      <TableCell>{stock.name}</TableCell>
                      <TableCell>${stock.price.toFixed(2)}</TableCell>
                      <TableCell className={stock.change >= 0 ? "text-green-600" : "text-red-600"}>
                        {stock.change >= 0 ? "+" : ""}{stock.change.toFixed(2)} ({stock.changePercent.toFixed(2)}%)
                      </TableCell>
                      <TableCell>
                        <Button 
                          size="sm" 
                          variant="outline"
                          onClick={() => router.push(`/stock/${stock.symbol}`)}
                        >
                          Trade
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <div className="mt-4">
                <Button 
                  variant="outline" 
                  className="w-full"
                  onClick={() => router.push("/watchlist")}
                >
                  View Full Watchlist
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;