// Stock Table Component
const StockTable = ({ stocks }) => (
    <div className="rounded-lg border bg-white">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left">Symbol</th>
              <th className="px-4 py-3 text-left">Company</th>
              <th className="px-4 py-3 text-right">Price</th>
              <th className="px-4 py-3 text-right">Change</th>
              <th className="px-4 py-3 text-right">Volume</th>
            </tr>
          </thead>
          <tbody>
            {stocks.map((stock) => (
              <tr key={stock.symbol} className="border-t">
                <td className="px-4 py-3 font-medium">{stock.symbol}</td>
                <td className="px-4 py-3">{stock.companyName}</td>
                <td className="px-4 py-3 text-right">${stock.currentPrice.toFixed(2)}</td>
                <td className={`px-4 py-3 text-right ${stock.percentageChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {stock.percentageChange >= 0 ? '+' : ''}{stock.percentageChange.toFixed(2)}%
                </td>
                <td className="px-4 py-3 text-right">{stock.volume.toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );

  export default StockTable;