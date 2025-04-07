// components/SearchBar.jsx
"use client";

import { useState } from "react";
import { Search, Star } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

const SearchBar = ({ isLoggedIn, router }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/stocks/search?stockName=${searchTerm}`);
      const data = await response.json();
      setSearchResults(data.body);
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

  return (
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
  );
};

export default SearchBar;