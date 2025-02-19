"use client";

import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowDown, ArrowUp, BarChart2, DollarSign, Percent, TrendingUp } from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import Navbar from '@/components/Navbar';
import StatsCard from '@/components/StatsCard';
import StockTable from '@/components/StockTable';







// Main Dashboard Component
const Dashboard = () => {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStocks = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/api/tracking', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch stocks');
        }
        
        const data = await response.json();
        setStocks(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchStocks();
    const interval = setInterval(fetchStocks, 60000); // Refresh every minute
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-red-600">Error: {error}</div>
      </div>
    );
  }

  const totalMarketCap = stocks.reduce((sum, stock) => sum + stock.marketCap, 0);
  const averageChange = stocks.reduce((sum, stock) => sum + stock.percentageChange, 0) / stocks.length;
  
  // Prepare data for the chart
  const chartData = stocks.map(stock => ({
    name: stock.symbol,
    price: stock.currentPrice,
    change: stock.percentageChange
  }));

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <main className="pt-16 px-4 sm:px-6 lg:px-8">
        <div className="py-6">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <StatsCard 
              title="Total Market Cap" 
              value={`$${(totalMarketCap / 1e12).toFixed(2)}T`}
              icon={DollarSign}
            />
            <StatsCard 
              title="Average Change" 
              value={`${averageChange >= 0 ? '+' : ''}${averageChange.toFixed(2)}%`}
              icon={Percent}
              trend={averageChange}
            />
            <StatsCard 
              title="Tracked Stocks" 
              value={stocks.length}
              icon={BarChart2}
            />
            <StatsCard 
              title="Total Volume" 
              value={stocks.reduce((sum, stock) => sum + stock.volume, 0).toLocaleString()}
              icon={TrendingUp}
            />
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Price Overview</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-[300px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Line type="monotone" dataKey="price" stroke="#2563eb" />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Performance</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="h-[300px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Line 
                        type="monotone" 
                        dataKey="change" 
                        stroke="#16a34a"
                        strokeWidth={2}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="mt-6">
            <Card>
              <CardHeader>
                <CardTitle>Stock Details</CardTitle>
              </CardHeader>
              <CardContent>
                <StockTable stocks={stocks} />
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;