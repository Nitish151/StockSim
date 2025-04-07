"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { LineChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart } from "recharts";
import Navbar from "@/components/Navbar";

const Portfolio = () => {
  const [stocks, setStocks] = useState();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    const fetchPortfolio = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch("http://localhost:8080/api/portfolio", {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!response.ok) throw new Error("Failed to fetch portfolio");
        const data = await response.json();
        setStocks(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchPortfolio();
  }, []);

  

  if (loading) return <div className="flex h-screen items-center justify-center">Loading...</div>;
  if (error) return <div className="text-red-600 text-center">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto p-6 lg:mt-8">
        <h1 className="text-2xl font-bold mb-6">My Investment Portfolio</h1>
        
        {/* Portfolio Summary */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Portfolio Summary</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-blue-50">
                <p className="text-sm text-gray-500">Total Investment</p>
                <p className="text-2xl font-bold">${stocks.totalInvested.toFixed(2)}</p>
              </div>
              <div className="p-4 rounded-lg bg-green-50">
                <p className="text-sm text-gray-500">Current Value</p>
                <p className="text-2xl font-bold">${stocks.currentValue.toFixed(2)}</p>
              </div>
              <div className={`p-4 rounded-lg ${stocks.totalProfitOrLoss >= 0 ? 'bg-green-50' : 'bg-red-50'}`}>
                <p className="text-sm text-gray-500">Profit/Loss</p>
                <p className={`text-2xl font-bold ${stocks.totalProfitOrLoss >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {stocks.totalProfitOrLoss >= 0 ? '+' : ''}${stocks.totalProfitOrLoss.toFixed(2)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Performance Chart */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Portfolio Allocation</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-[300px]">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={stocks.holdings.map(stock => ({
                  name: stock.stockSymbol,
                  value: stock.currentValue,
                  investment: stock.totalInvestment
                }))}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip 
                    formatter={(value) => [`$${value.toFixed(2)}`, 'Amount']}
                    labelFormatter={(label) => `${label}`}
                  />
                  <Bar dataKey="value" name="Current Value" fill="#4ade80" />
                  <Bar dataKey="investment" name="Investment" fill="#60a5fa" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Stock Table */}
        <Card>
          <CardHeader>
            <CardTitle>Holdings</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Company</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Quantity</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg. Buy Price</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Current Price</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Value</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Profit/Loss</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {stocks.holdings.map((stock) => (
                    <tr key={stock.id}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div>
                            <div className="text-sm font-medium text-gray-900">{stock.stockSymbol}</div>
                            <div className="text-sm text-gray-500">{stock.companyName}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{stock.quantity}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${stock.avgBuyPrice.toFixed(2)}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${stock.currentPrice.toFixed(2)}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${stock.currentValue.toFixed(2)}</td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex text-sm rounded-full ${stock.profitOrLoss >= 0 ? 'text-green-800 bg-green-100' : 'text-red-800 bg-red-100'} px-2 py-1`}>
                          {stock.profitOrLoss >= 0 ? '+' : ''}${stock.profitOrLoss.toFixed(2)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Portfolio;