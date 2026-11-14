import { useContext, useEffect } from "react";
import "./App.css";
import { AuthContext } from "react-oauth2-code-pkce";

function App() {
  const { token, tokenData, logIn, logOut } =
    useContext(AuthContext);

  const isAuthenticated = !!token;

  console.log("isAuthenticated:", isAuthenticated);

  useEffect(() => {
    if (token) {
      console.log("Access Token:", token);
      console.log("Token Data:", tokenData);
    }
  }, [token, tokenData]);

  const callBackend = async () => {
    if (!token) {
      alert("You are not logged in!");
      return;
    }

    try {
      const response = await fetch(
        "http://localhost:8080/api/secure",
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Backend error:", errorText);
        alert("Backend returned error: " + response.status);
        return;
      }

      const text = await response.text();
      alert("Backend says: " + text);
    } catch (error) {
      console.error("API error:", error);
      alert("Network error while calling backend");
    }
  };

  return (
    <div style={{ padding: "2rem" }}>
      <h1>OAuth PKCE Demo</h1>

      <div style={{ marginBottom: "1rem" }}>
        {!isAuthenticated ? (
          <button onClick={logIn}>Login</button>
        ) : (
          <button onClick={logOut}>Logout</button>
        )}
      </div>

      {isAuthenticated && (
        <div>
          <p>
            Logged in as:{" "}
            <strong>{tokenData?.preferred_username}</strong>
          </p>

          <button onClick={callBackend}>
            Call Secure Backend API
          </button>
        </div>
      )}
    </div>
  );
}

export default App;
