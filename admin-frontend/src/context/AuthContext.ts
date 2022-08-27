import React from "react";

interface AuthContextType {
  isAuthenticated: boolean;
  setIsAuthenticated: (value: boolean) => void;
}

export default React.createContext<AuthContextType>({
  isAuthenticated: false,
  setIsAuthenticated: () => {},
});
