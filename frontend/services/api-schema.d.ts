// Fallback type definition to satisfy Deno Check during development
// before `generate-client` is successfully executed against a live Java Backend instance.

export interface paths {
  // A hypothetical market data path exported by Java MicroProfile:
  "/v1/market/prices": {
     get: {
       parameters: {
         query: { instrument?: string };
       };
       responses: {
         200: {
           content: {
             "application/json": {
               currentPrice: number;
               instrument: string;
             }[];
           };
         };
       };
     };
  };
  "/v1/trading/orders": {
     post: {
       requestBody: {
         content: {
           "application/json": {
             type: "MARKET" | "LIMIT" | "STOP";
             direction: "BUY" | "SELL";
             quantity: number;
             instrument: string;
             price?: number;
           };
         };
       };
       responses: {
         201: {
           content: {
             "application/json": {
               orderId: string;
               status: "PENDING" | "EXECUTED" | "REJECTED";
             };
         };
       };
     };
  };
}
};

