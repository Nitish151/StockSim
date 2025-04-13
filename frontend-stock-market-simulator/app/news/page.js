"use client";

import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { Search } from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from "@/components/ui/pagination";
import Navbar from '@/components/Navbar';

const NewsPage = () => {
  const searchParams = useSearchParams();

  // Extract filters from URL on first render
  const initialTicker = searchParams.get('ticker') || '';
  const initialType = searchParams.get('type') || 'ALL';

  // Main state
  const [allNews, setAllNews] = useState([]);
  const [displayedNews, setDisplayedNews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filter state
  const [tickerFilter, setTickerFilter] = useState(initialTicker);
  const [typeFilter, setTypeFilter] = useState(initialType);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 9; // Changed to 9 items per page for better grid layout

  const fetchNews = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      if (!token) throw new Error('Authentication token not found');

      let url = 'http://localhost:8080/api/stocks/news';
      const params = new URLSearchParams();

      if (tickerFilter) params.append('tickers', tickerFilter);
      if (typeFilter && typeFilter !== 'ALL') params.append('type', typeFilter);

      const queryString = params.toString();
      if (queryString) url += `?${queryString}`;

      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        if (response.status === 401) throw new Error('Authentication failed. Please log in again.');
        throw new Error('Failed to fetch news');
      }

      const data = await response.json();
      const newsData = data.body || [];

      setAllNews(newsData);
      setCurrentPage(1);
      updateDisplayedNews(newsData, 1);
    } catch (err) {
      setError(err.message);
      setAllNews([]);
      setDisplayedNews([]);
    } finally {
      setLoading(false);
    }
  };

  const updateDisplayedNews = (newsArray, page) => {
    const startIndex = (page - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    setDisplayedNews(newsArray.slice(startIndex, endIndex));
  };

  // Initial fetch on mount
  useEffect(() => {
    fetchNews();
  }, []);

  // Refresh display on pagination
  useEffect(() => {
    updateDisplayedNews(allNews, currentPage);
  }, [currentPage, allNews]);

  const handleSearch = () => {
    fetchNews();
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
    // Scroll to top smoothly when changing pages
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleTickerChange = (e) => {
    setTickerFilter(e.target.value);
  };

  const handleTypeChange = (value) => {
    setTypeFilter(value);
  };

  const formatTime = (timeStr, agoStr) => {
    return agoStr || timeStr;
  };

  const totalPages = Math.ceil(allNews.length / itemsPerPage);

  const getPaginationItems = () => {
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);
    if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);
    return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Market News</h1>

        {/* Filters */}
        <div className="bg-white p-6 rounded-lg shadow-sm mb-8">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Filter News</h2>
          <div className="flex flex-col md:flex-row gap-4">
            <div className="relative flex-1">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search className="h-5 w-5 text-gray-400" />
              </div>
              <Input
                type="text"
                placeholder="Filter by ticker symbol (e.g., AAPL, MSFT)"
                className="pl-10 w-full"
                value={tickerFilter}
                onChange={handleTickerChange}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleSearch();
                }}
              />
            </div>

            <div className="w-full md:w-64">
              <Select value={typeFilter} onValueChange={handleTypeChange}>
                <SelectTrigger>
                  <SelectValue placeholder="Select content type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Content Types</SelectItem>
                  <SelectItem value="Article">Articles</SelectItem>
                  <SelectItem value="Video">Videos</SelectItem>
                  <SelectItem value="PRESS_RELEASE">Press Releases</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <Button className="bg-blue-600 hover:bg-blue-700 text-white" onClick={handleSearch}>
              Search
            </Button>
          </div>
        </div>

        {/* News List */}
        {loading ? (
          <div className="flex justify-center items-center h-64 bg-white rounded-lg shadow-sm">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-t-blue-500 border-b-blue-500 rounded-full animate-spin mx-auto mb-4"></div>
              <p className="text-lg">Loading news...</p>
            </div>
          </div>
        ) : error ? (
          <div className="bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded-lg shadow-sm" role="alert">
            <strong className="font-bold">Error: </strong>
            <span className="block sm:inline">{error}</span>
          </div>
        ) : allNews.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-lg shadow-sm">
            <p className="text-xl text-gray-600">No news articles found for the current filters.</p>
            <p className="text-gray-500 mt-2">Try adjusting your search criteria.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-12">
              {displayedNews.map((item, index) => (
                <div key={index} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300 h-full flex flex-col">
                  {item.img && (
                    <div className="h-48 overflow-hidden">
                      <img
                        src={item.img}
                        alt={item.title}
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          e.target.src = "/api/placeholder/400/300";
                          e.target.alt = "Image unavailable";
                        }}
                      />
                    </div>
                  )}
                  <div className="p-6 flex flex-col flex-grow">
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-sm font-medium text-blue-600">{item.source}</span>
                      <span className="text-xs text-gray-500">{formatTime(item.time, item.ago)}</span>
                    </div>
                    <h2 className="text-xl font-semibold mb-3 line-clamp-2">{item.title}</h2>
                    <p className="text-gray-600 mb-5 line-clamp-3 flex-grow">{item.text}</p>
                    {item.tickers && item.tickers.length > 0 && (
                      <div className="flex flex-wrap gap-1 mb-4">
                        {item.tickers.map((ticker, idx) => (
                          <span key={idx} className="inline-block bg-gray-100 px-2 py-1 text-xs font-semibold text-gray-800 rounded">
                            {ticker.replace('#', '')}
                          </span>
                        ))}
                      </div>
                    )}
                    <a
                      href={item.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded text-sm transition-colors duration-300 mt-auto"
                    >
                      Read More
                    </a>
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex flex-col items-center mb-8 bg-white p-6 rounded-lg shadow-sm">
                <div className="text-sm text-gray-600 mb-4">
                  Showing {(currentPage - 1) * itemsPerPage + 1} to {Math.min(currentPage * itemsPerPage, allNews.length)} of {allNews.length} news items
                </div>
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => currentPage > 1 && handlePageChange(currentPage - 1)}
                        className={currentPage <= 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                      />
                    </PaginationItem>
                    {getPaginationItems().map(page => (
                      <PaginationItem key={page}>
                        <PaginationLink
                          isActive={page === currentPage}
                          onClick={() => handlePageChange(page)}
                        >
                          {page}
                        </PaginationLink>
                      </PaginationItem>
                    ))}
                    <PaginationItem>
                      <PaginationNext
                        onClick={() => currentPage < totalPages && handlePageChange(currentPage + 1)}
                        className={currentPage >= totalPages ? "pointer-events-none opacity-50" : "cursor-pointer"}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default NewsPage;