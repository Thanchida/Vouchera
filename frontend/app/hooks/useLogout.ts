import { useNavigate } from "react-router";
import { request } from "~/lib/api-client";
import { error as toastError } from "~/lib/toast";

export function useLogout() {
  const navigate = useNavigate();

  const logout = async () => {
    try {
      await request("/api/auth/logout", {
        method: "POST"
      });

      navigate("/login");
    } catch (error) {
      console.error("Logout failed:", error);
      toastError("Logout failed. Please try again.");
    }
  };

  return { logout };
}