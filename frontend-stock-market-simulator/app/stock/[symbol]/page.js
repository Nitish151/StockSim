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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import {
  TrendingUp,
  TrendingDown,
  Clock,
  Star,
  StarOff,
  Info,
  ArrowUpRight,
  DollarSign,
  BarChart2,
} from "lucide-react";
import Navbar from "@/components/Navbar";

const StockDetailPage = ({ params }) => {
  const router = useRouter();
  const [stockData, setStockData] = useState(null);
  const [chartData, setChartData] = useState([]);
  const [timeRange, setTimeRange] = useState("1W");
  const [isWatchlisted, setIsWatchlisted] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [tradeType, setTradeType] = useState("buy");
  const [quantity, setQuantity] = useState(1);
  const [orderType, setOrderType] = useState("market");
  const [limitPrice, setLimitPrice] = useState("");
  const [newsItems, setNewsItems] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Get stock symbol from URL parameter
  const stockSymbol = params?.symbol || "AAPL";

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem("token");
    setIsLoggedIn(!!token);

    fetchStockData();
    fetchChartData(timeRange);
    fetchStockNews();

    if (token) {
      checkWatchlistStatus();
    }
  }, [stockSymbol, timeRange]);

  const fetchStockData = async () => {
    try {
      setIsLoading(true);
      const response = await fetch(
        `http://localhost:8080/api/stocks/${stockSymbol}`
      );
      console.log("Response:", response);
      const data = await response.json();
      console.log("Stock Data:", data);
      setStockData(data);
    } catch (error) {
      console.error("Error fetching stock data:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchChartData = async (range) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/stocks/${stockSymbol}/chart?range=${range}`
      );
      const data = await response.json();
      setChartData(data);
    } catch (error) {
      console.error("Error fetching chart data:", error);
    }
  };

  const fetchStockNews = async () => {
    try {
      // Use the stock symbol from the URL parameter
      const response = await fetch(
        `http://localhost:8080/api/stocks/news?tickers=${stockSymbol}`
      );
      const data = await response.json();

      // Format the news data to match your component's expected structure
      const formattedNews = data.body.map((item, index) => ({
        id: index,
        title: item.title,
        source: item.source,
        date: item.time,
        url: item.url,
      }));

      setNewsItems(formattedNews);
    } catch (error) {
      console.error("Error fetching stock news:", error);
      // If the API call fails, you can still fall back to your mock data
      setNewsItems(mockNewsItems);
    }
  };

  const checkWatchlistStatus = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/watchlist/check/${stockSymbol}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      const data = await response.json();
      setIsWatchlisted(data.isWatchlisted);
    } catch (error) {
      console.error("Error checking watchlist status:", error);
    }
  };

  const handleWatchlistToggle = async () => {
    if (!isLoggedIn) {
      router.push("/login");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const endpoint = isWatchlisted ? "remove" : "add";

      await fetch(`http://localhost:8080/api/watchlist/${endpoint}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ symbol: stockSymbol }),
      });

      setIsWatchlisted(!isWatchlisted);
    } catch (error) {
      console.error("Error updating watchlist:", error);
    }
  };

  const handleTradeSubmit = async (e) => {
    e.preventDefault();

    if (!isLoggedIn) {
      router.push("/login");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      await fetch("http://localhost:8080/api/trades", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          symbol: stockSymbol,
          type: tradeType.toUpperCase(),
          quantity: parseInt(quantity),
          orderType: orderType,
          limitPrice: orderType === "limit" ? parseFloat(limitPrice) : null,
        }),
      });

      alert(
        `${tradeType.toUpperCase()} order for ${quantity} shares of ${stockSymbol} placed successfully!`
      );
      setQuantity(1);
      setLimitPrice("");
    } catch (error) {
      console.error("Error placing trade:", error);
      alert("Failed to place trade. Please try again.");
    }
  };

  const calculateOrderTotal = () => {
    if (!stockData) return 0;
    const price =
      orderType === "limit" && limitPrice
        ? parseFloat(limitPrice)
        : stockData.regularMarketPrice;
    return (price * quantity).toFixed(2);
  };

  // Mock data for demonstration
  const mockStockData = {
    symbol: stockSymbol,
    name:
      stockSymbol === "AAPL"
        ? "Apple Inc."
        : stockSymbol === "MSFT"
        ? "Microsoft Corporation"
        : "Google Inc.",
    price: 239.51,
    change: 2.34,
    changePercent: 0.99,
    open: 237.17,
    high: 240.12,
    low: 236.89,
    volume: 35418792,
    marketCap: "3.78T",
    peRatio: 29.45,
    dividend: 0.96,
    dividendYield: 0.4,
    eps: 8.13,
    week52High: 245.17,
    week52Low: 167.34,
  };

  const mockChartData = [
    { date: "2025-03-17", value: 235.21 },
    { date: "2025-03-18", value: 236.4 },
    { date: "2025-03-19", value: 234.89 },
    { date: "2025-03-20", value: 237.22 },
    { date: "2025-03-21", value: 239.51 },
  ];

  const mockNewsItems = [
    {
      id: 1,
      title: `${mockStockData.name} Reports Strong Quarterly Earnings`,
      source: "Financial Times",
      date: "2025-03-21",
      url: "#",
    },
    {
      id: 2,
      title: `Analysts Raise Price Target for ${mockStockData.symbol}`,
      source: "Bloomberg",
      date: "2025-03-20",
      url: "#",
    },
    {
      id: 3,
      title: `${mockStockData.name} Announces New Product Line`,
      source: "CNBC",
      date: "2025-03-19",
      url: "#",
    },
  ];

  // Use mock data if API calls are not implemented
  useEffect(() => {
    if (!stockData && !isLoading) {
      setStockData(mockStockData);
    }
    if (chartData.length === 0 && !isLoading) {
      setChartData(mockChartData);
    }
    if (newsItems.length === 0 && !isLoading) {
      setNewsItems(mockNewsItems);
    }
  }, [isLoading]);

  if (isLoading && !stockData) {
    return (
      <div className="flex h-screen items-center justify-center">
        Loading...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 lg: mt-16">
        {/* Stock Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8">
          <div>
            <div className="flex items-center">
              <h1 className="text-2xl font-bold">{stockData?.symbol}</h1>
              <Button
                variant="ghost"
                size="icon"
                className="ml-2"
                onClick={handleWatchlistToggle}
              >
                {isWatchlisted ? (
                  <Star className="text-yellow-500" size={20} />
                ) : (
                  <StarOff size={20} />
                )}
              </Button>
            </div>
            <p className="text-gray-600">{stockData?.longName}</p>
          </div>
          <div className="mt-4 md:mt-0">
            <p className="text-3xl font-bold">
              ${stockData?.regularMarketPrice?.toFixed(2)}
            </p>
            <div className="flex items-center">
              {stockData?.regularMarketChange >= 0 ? (
                <ArrowUpRight className="text-green-600 mr-1" size={16} />
              ) : (
                <TrendingDown className="text-red-600 mr-1" size={16} />
              )}
              <span
                className={
                  stockData?.regularMarketChange >= 0
                    ? "text-green-600"
                    : "text-red-600"
                }
              >
                {stockData?.regularMarketChange >= 0 ? "+" : ""}
                {stockData?.regularMarketChange?.toFixed(2)} (
                {stockData?.regularMarketChangePercent?.toFixed(2)}%)
              </span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column - Chart and Info */}
          <div className="lg:col-span-2 space-y-8">
            {/* Price Chart */}
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <CardTitle>Price Chart</CardTitle>
                  <div className="flex space-x-2">
                    {["1D", "1W", "1M", "3M", "1Y", "5Y"].map((range) => (
                      <Button
                        key={range}
                        variant={timeRange === range ? "default" : "outline"}
                        size="sm"
                        onClick={() => setTimeRange(range)}
                      >
                        {range}
                      </Button>
                    ))}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="h-[400px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart
                      data={chartData}
                      margin={{ top: 10, right: 30, left: 0, bottom: 0 }}
                    >
                      <defs>
                        <linearGradient
                          id="colorValue"
                          x1="0"
                          y1="0"
                          x2="0"
                          y2="1"
                        >
                          <stop
                            offset="5%"
                            stopColor="#3b82f6"
                            stopOpacity={0.8}
                          />
                          <stop
                            offset="95%"
                            stopColor="#3b82f6"
                            stopOpacity={0}
                          />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis
                        dataKey="date"
                        tickFormatter={(date) => {
                          const d = new Date(date);
                          return `${d.getMonth() + 1}/${d.getDate()}`;
                        }}
                      />
                      <YAxis
                        domain={["auto", "auto"]}
                        tickFormatter={(value) => `$${value}`}
                      />
                      <Tooltip
                        formatter={(value) => [`$${value.toFixed(2)}`, "Price"]}
                        labelFormatter={(label) => {
                          const d = new Date(label);
                          return d.toLocaleDateString();
                        }}
                      />
                      <Area
                        type="monotone"
                        dataKey="value"
                        stroke="#3b82f6"
                        fillOpacity={1}
                        fill="url(#colorValue)"
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            {/* Stock Info */}
            <Card>
              <CardHeader>
                <CardTitle>Stock Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                  <div>
                    <p className="text-sm text-gray-500">Open</p>
                    <p className="font-medium">
                      ${stockData?.regularMarketOpen?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">High</p>
                    <p className="font-medium">
                      ${stockData?.regularMarketDayHigh?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Low</p>
                    <p className="font-medium">
                      ${stockData?.regularMarketDayLow?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Volume</p>
                    <p className="font-medium">
                      {stockData?.regularMarketVolume?.toLocaleString()}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Market Cap</p>
                    <p className="font-medium">{stockData?.marketCap}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Trailing P/E</p>
                    <p className="font-medium">
                      {stockData?.trailingPE?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">
                      EPS Trailing 12 months
                    </p>
                    <p className="font-medium">
                      ${stockData?.epsTrailingTwelveMonths?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Dividend Yield</p>
                    <p className="font-medium">
                      {stockData?.dividendYield?.toFixed(2)}%
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">52W High</p>
                    <p className="font-medium">
                      ${stockData?.fiftyTwoWeekHigh?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">52W Low</p>
                    <p className="font-medium">
                      ${stockData?.fiftyTwoWeekLow?.toFixed(2)}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* News */}
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <CardTitle>Recent News</CardTitle>
                  <Button
                    variant="link"
                    onClick={() => router.push(`/news?ticker=${stockSymbol}`)}
                    className="text-sm"
                  >
                    View All
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                {newsItems.length > 0 ? (
                  <ul className="space-y-4">
                    {newsItems.slice(0, 5).map((news) => (
                      <li
                        key={news.id}
                        className="border-b pb-4 last:border-0 last:pb-0"
                      >
                        <a
                          href={news.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="block hover:bg-gray-50 rounded-md p-2 -m-2 transition-colors"
                        >
                          <h3 className="font-medium text-blue-600 hover:text-blue-800 mb-1">
                            {news.title}
                          </h3>
                          <div className="flex items-center text-sm text-gray-500">
                            <span>{news.source}</span>
                            <span className="mx-2">•</span>
                            <span>{news.date}</span>
                          </div>
                        </a>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-center text-gray-500 py-4">
                    No recent news available
                  </p>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Right Column - Trade and Analysis */}
          <div className="space-y-8">
            {/* Trade Card */}
            <Card>
              <CardHeader>
                <CardTitle>Trade {stockData?.symbol}</CardTitle>
                <CardDescription>
                  Place an order for {stockData?.name}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleTradeSubmit} className="space-y-4">
                  <Tabs defaultValue="buy" onValueChange={setTradeType}>
                    <TabsList className="grid grid-cols-2 w-full">
                      <TabsTrigger value="buy">Buy</TabsTrigger>
                      <TabsTrigger value="sell">Sell</TabsTrigger>
                    </TabsList>
                  </Tabs>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Quantity</label>
                    <Input
                      type="number"
                      min="1"
                      value={quantity}
                      onChange={(e) => setQuantity(e.target.value)}
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium">Order Type</label>
                    <Select value={orderType} onValueChange={setOrderType}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select order type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="market">Market Order</SelectItem>
                        <SelectItem value="limit">Limit Order</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {orderType === "limit" && (
                    <div className="space-y-2">
                      <label className="text-sm font-medium">Limit Price</label>
                      <Input
                        type="number"
                        step="0.01"
                        value={limitPrice}
                        onChange={(e) => setLimitPrice(e.target.value)}
                        required
                      />
                    </div>
                  )}

                  <div className="py-2 border-t border-b">
                    <div className="flex justify-between items-center">
                      <span className="font-medium">Estimated Total:</span>
                      <span className="font-bold text-lg">
                        ${calculateOrderTotal()}
                      </span>
                    </div>
                  </div>

                  <Button type="submit" className="w-full">
                    {tradeType === "buy" ? "Buy" : "Sell"} {stockData?.symbol}
                  </Button>

                  {!isLoggedIn && (
                    <p className="text-sm text-center text-gray-500">
                      <a
                        href="/login"
                        className="text-blue-600 hover:underline"
                      >
                        Log in
                      </a>{" "}
                      to place trades
                    </p>
                  )}
                </form>
              </CardContent>
            </Card>

            {/* Analysis */}
            <Card>
              <CardHeader>
                <CardTitle>Analyst Consensus</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center justify-center space-x-8">
                    <div className="text-center">
                      <p className="text-sm text-gray-500">Buy</p>
                      <p className="font-bold text-xl text-green-600">75%</p>
                    </div>
                    <div className="text-center">
                      <p className="text-sm text-gray-500">Hold</p>
                      <p className="font-bold text-xl text-gray-600">20%</p>
                    </div>
                    <div className="text-center">
                      <p className="text-sm text-gray-500">Sell</p>
                      <p className="font-bold text-xl text-red-600">5%</p>
                    </div>
                  </div>

                  <div className="pt-4 border-t">
                    <div className="flex justify-between items-center mb-2">
                      <p className="text-sm font-medium">Price Target</p>
                      <p className="font-bold">
                        ${(stockData?.price * 1.15).toFixed(2)}
                      </p>
                    </div>
                    <div className="text-sm text-gray-500">
                      <span className="font-medium text-green-600">+15%</span>{" "}
                      upside potential
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Similar Stocks */}
            <Card>
              <CardHeader>
                <CardTitle>Similar Stocks</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3">
                  {["MSFT", "GOOG", "AMZN", "META"]
                    .filter((sym) => sym !== stockSymbol)
                    .slice(0, 3)
                    .map((symbol) => (
                      <li key={symbol} className="border rounded-md p-3">
                        <a
                          href={`/stocks/${symbol}`}
                          className="flex justify-between items-center"
                        >
                          <div>
                            <p className="font-medium">{symbol}</p>
                            <p className="text-sm text-gray-500">
                              {symbol === "MSFT"
                                ? "Microsoft Corporation"
                                : symbol === "GOOG"
                                ? "Alphabet Inc."
                                : symbol === "AMZN"
                                ? "Amazon.com Inc."
                                : "Meta Platforms Inc."}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-medium">
                              ${(Math.random() * 100 + 200).toFixed(2)}
                            </p>
                            <p
                              className={
                                Math.random() > 0.5
                                  ? "text-green-600 text-sm"
                                  : "text-red-600 text-sm"
                              }
                            >
                              {Math.random() > 0.5 ? "+" : "-"}
                              {(Math.random() * 2).toFixed(2)}%
                            </p>
                          </div>
                        </a>
                      </li>
                    ))}
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
};

export default StockDetailPage;
