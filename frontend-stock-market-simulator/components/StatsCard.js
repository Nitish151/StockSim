import { ArrowDown, ArrowUp } from "lucide-react";
import { Card
    , CardHeader
    , CardTitle
    , CardContent
 } from "./ui/card";
// Stats Card Component
const StatsCard = ({ title, value, icon: Icon, trend }) => (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <Icon className="h-4 w-4 text-gray-500" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {trend && (
          <div className={`flex items-center text-sm ${trend >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            {trend >= 0 ? <ArrowUp className="h-4 w-4 mr-1" /> : <ArrowDown className="h-4 w-4 mr-1" />}
            {Math.abs(trend).toFixed(2)}%
          </div>
        )}
      </CardContent>
    </Card>
  );

  export default StatsCard;