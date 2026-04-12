import { toast as hotToast } from "react-hot-toast";

function normalizeMessage(message: string) {
	const trimmed = message.trim();
	return trimmed || "Something went wrong.";
}

export function success(message: string) {
	return hotToast.success(normalizeMessage(message), {
		duration: 2500,
	});
}

export function error(message: string) {
	return hotToast.error(normalizeMessage(message), {
		duration: 4000,
	});
}