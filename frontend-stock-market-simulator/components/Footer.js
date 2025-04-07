// components/Footer.jsx

const Footer = () => {
    return (
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
    );
  };
  
  export default Footer;