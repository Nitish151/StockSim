import { TrendingUp } from "lucide-react";

const Navbar = () => (
    <nav className="bg-white border-b border-gray-200 fixed w-full z-30 top-0">
      <div className="px-3 py-3 lg:px-5 lg:pl-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <TrendingUp className="h-8 w-8 text-blue-600" />
            <span className="ml-2 text-xl font-semibold">StockTracker</span>
          </div>
          <div className="flex items-center">
            <span className="text-sm text-gray-500">Last Updated: {new Date().toLocaleString()}</span>
          </div>
        </div>
      </div>
    </nav>
  );

export default Navbar