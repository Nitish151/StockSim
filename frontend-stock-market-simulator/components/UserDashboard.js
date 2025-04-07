// components/UserDashboard.jsx
"use client";

import { BarChart3 } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

const UserDashboard = ({ router }) => {
  return (
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
  );
};

export default UserDashboard;
