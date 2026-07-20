import * as React from "react";
import { Route, Switch, Router as WouterRouter } from "wouter";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Layout } from "./components/layout";
import { initAuth } from "./lib/auth";

import Dashboard from "./pages/dashboard";
import Login from "./pages/login";
import Register from "./pages/register";
import ParcelsList from "./pages/parcels/index";
import ParcelNew from "./pages/parcels/new";
import ParcelDetail from "./pages/parcels/[id]";
import RidesList from "./pages/rides/index";
import RideNew from "./pages/rides/new";
import RideDetail from "./pages/rides/[id]";
import TicketsList from "./pages/tickets";
import Profile from "./pages/profile";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

initAuth();

function AppRouter() {
  return (
    <Layout>
      <Switch>
        <Route path="/" component={Dashboard} />
        <Route path="/parcels" component={ParcelsList} />
        <Route path="/parcels/new" component={ParcelNew} />
        <Route path="/parcels/:id" component={ParcelDetail} />
        <Route path="/rides" component={RidesList} />
        <Route path="/rides/new" component={RideNew} />
        <Route path="/rides/:id" component={RideDetail} />
        <Route path="/tickets" component={TicketsList} />
        <Route path="/profile" component={Profile} />
        <Route>
          <div className="flex h-[50vh] items-center justify-center flex-col">
            <h1 className="text-4xl font-bold text-muted-foreground">404</h1>
            <p className="text-muted-foreground mt-2">Page not found</p>
          </div>
        </Route>
      </Switch>
    </Layout>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <WouterRouter base={import.meta.env.BASE_URL.replace(/\/$/, "")}>
        <Switch>
          <Route path="/login" component={Login} />
          <Route path="/register" component={Register} />
          <Route path="/:rest*" component={AppRouter} />
        </Switch>
      </WouterRouter>
    </QueryClientProvider>
  );
}

export default App;
