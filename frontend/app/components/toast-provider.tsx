import { useEffect, useState } from "react";
import { Toaster } from "react-hot-toast";

export function ToastProvider() {
	const [mounted, setMounted] = useState(false);

	useEffect(() => {
		setMounted(true);
	}, []);

	if (!mounted) {
		return null;
	}

	return (
		<Toaster
			position="top-right"
			toastOptions={{
				duration: 3000,
				style: {
					borderRadius: "14px",
					border: "1px solid #d4d4d8",
					background: "#ffffff",
					color: "#18181b",
					boxShadow: "0 12px 30px rgba(24, 24, 27, 0.12)",
				},
				success: {
					duration: 2500,
					style: {
						border: "1px solid #bbf7d0",
					},
				},
				error: {
					duration: 4000,
					style: {
						border: "1px solid #fecaca",
					},
				},
			}}
		/>
	);
}