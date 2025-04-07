// components/MarketSummary.jsx
"use client";

import { Card, CardContent } from "@/components/ui/card";

const MarketSummary = ({ marketSummary }) => {
  // Default to empty object if marketSummary is not provided
  const summary = marketSummary || {};
  
  if (Object.keys(summary).length === 0) {
    return null;
  }
  
  return (
    <div className="mb-8">
      <h2 className="text-xl font-bold mb-4">Market Summary</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="pt-6">
            <h3 className="text-lg font-medium mb-1">Dow Jones</h3>
            <p className="text-2xl font-bold">{summary.dowJones.value.toLocaleString()}</p>
            <p className={summary.dowJones.change >= 0 ? "text-green-600" : "text-red-600"}>
              {summary.dowJones.change >= 0 ? "+" : ""}{summary.dowJones.change.toFixed(2)} ({summary.dowJones.changePercent.toFixed(2)}%)
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <h3 className="text-lg font-medium mb-1">NASDAQ</h3>
            <p className="text-2xl font-bold">{summary.nasdaq.value.toLocaleString()}</p>
            <p className={summary.nasdaq.change >= 0 ? "text-green-600" : "text-red-600"}>
              {summary.nasdaq.change >= 0 ? "+" : ""}{summary.nasdaq.change.toFixed(2)} ({summary.nasdaq.changePercent.toFixed(2)}%)
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <h3 className="text-lg font-medium mb-1">S&P 500</h3>
            <p className="text-2xl font-bold">{summary.sp500.value.toLocaleString()}</p>
            <p className={summary.sp500.change >= 0 ? "text-green-600" : "text-red-600"}>
              {summary.sp500.change >= 0 ? "+" : ""}{summary.sp500.change.toFixed(2)} ({summary.sp500.changePercent.toFixed(2)}%)
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default MarketSummary;