"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import Navbar from "@/components/Navbar";

const StockDetails = ({params}) => {
  const router = useRouter();

//   const { symbol } = router.query;
  const symbol = params.symbol;
  const [stock, setStock] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tracking, setTracking] = useState(false);

  useEffect(() => {
    if (!symbol) return;

    const fetchStockDetails = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch(`http://localhost:8080/api/stocks/${symbol}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!response.ok) throw new Error("Failed to fetch stock details");
        const data = await response.json();
        setStock(data);
        setTracking(data.isTracked);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchStockDetails();
  }, [symbol]);

  const handleTrackingToggle = async () => {
    const token = localStorage.getItem("token");
    const url = `http://localhost:8080/api/tracking/${symbol}`;
    const method = tracking ? "DELETE" : "POST";
    try {
      await fetch(url, {
        method,
        headers: { Authorization: `Bearer ${token}` },
      });
      setTracking(!tracking);
    } catch (err) {
      console.error("Failed to update tracking status", err);
    }
  };

  if (loading) return <div className="flex h-screen items-center justify-center">Loading...</div>;
  if (error) return <div className="text-red-600 text-center">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-4xl mx-auto p-6 mt-20">
        <Card>
          <CardHeader>
            <CardTitle>{stock.companyName} ({stock.symbol})</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-700">Industry: {stock.industry}</p>
            <p className="text-lg font-semibold">Current Price: ${stock.currentPrice}</p>
            <p className={`text-${stock.percentageChange >= 0 ? "green" : "red"}-500 font-bold`}>
              {stock.percentageChange >= 0 ? "+" : ""}{stock.percentageChange.toFixed(2)}%
            </p>
            <Button onClick={handleTrackingToggle} className="mt-4">
              {tracking ? "Untrack Stock" : "Track Stock"}
            </Button>
          </CardContent>
        </Card>

        <div className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Stock Price History</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-[300px]">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={stock.history}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="price" stroke="#2563eb" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default StockDetails;
