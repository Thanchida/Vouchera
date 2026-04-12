import { useState } from "react";
import { useNavigate } from "react-router";
import { request } from "~/lib/api-client";

type FormData = {
  email: string;
  password: string;
  confirmPassword: string;
};

type FormErrors = Partial<Record<keyof FormData, string>>;

export function meta() {
  return [
    { title: "Register | Vouchera" },
    { name: "description", content: "Create your Vouchera account." },
  ];
}

const initialForm: FormData = {
  email: "",
  password: "",
  confirmPassword: "",
};

export default function RegisterPage() {
  const [form, setForm] = useState<FormData>(initialForm);
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitError, setSubmitError] = useState("");
  const [submitSuccess, setSubmitSuccess] = useState("");
  const navigate = useNavigate();

  function setField<K extends keyof FormData>(key: K, value: FormData[K]) {
    setForm(prev => ({ ...prev, [key]: value }));
    setErrors(prev => ({ ...prev, [key]: undefined }));
  }

  function validate(values: FormData): FormErrors {
    const nextErrors: FormErrors = {};

    if (!values.email.trim()) {
      nextErrors.email = "Email is required.";
    } else if (!/^\S+@\S+\.\S+$/.test(values.email)) {
      nextErrors.email = "Enter a valid email address.";
    }

    if (!values.password) {
      nextErrors.password = "Password is required.";
    } else if (values.password.length < 8) {
      nextErrors.password = "Password must be at least 8 characters.";
    }

    if (!values.confirmPassword) {
      nextErrors.confirmPassword = "Please confirm your password.";
    } else if (values.confirmPassword !== values.password) {
      nextErrors.confirmPassword = "Passwords do not match.";
    }

    return nextErrors;
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validate(form);
    setErrors(nextErrors);
    setSubmitError("");
    setSubmitSuccess("");

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    void (async () => {
      try {
        await request("/api/users/register", {
          method: "POST",
          body: JSON.stringify({
            email: form.email,
            password: form.password,
          }),
        });

        setSubmitSuccess("Registration successful. Redirecting to login...");
        setTimeout(() => navigate("/login"), 700);
      } catch (error) {
        setSubmitError(error instanceof Error ? error.message : "Registration failed.");
      }
    })();
  }

  return (
    <main className="min-h-screen bg-white px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto flex min-h-[calc(100vh-6rem)] w-full max-w-5xl items-center justify-center">
        <section className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-7 shadow-sm sm:p-8">
          <h1 className="text-2xl font-semibold tracking-tight text-zinc-950">Create an Account</h1>
          <p className="mt-2 text-sm text-zinc-600">Sign up to get started</p>

          <form className="mt-7 space-y-4" onSubmit={handleSubmit} noValidate>
            {submitError ? (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {submitError}
              </div>
            ) : null}
            {submitSuccess ? (
              <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                {submitSuccess}
              </div>
            ) : null}

            <div>
              <label htmlFor="email" className="mb-1.5 block text-sm font-medium text-zinc-800">
                Email
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                value={form.email}
                onChange={event => setField("email", event.target.value)}
                className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              />
              {errors.email ? <p className="mt-1.5 text-xs text-red-600">{errors.email}</p> : null}
            </div>

            <div>
              <label htmlFor="password" className="mb-1.5 block text-sm font-medium text-zinc-800">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="new-password"
                value={form.password}
                onChange={event => setField("password", event.target.value)}
                className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              />
              {errors.password ? <p className="mt-1.5 text-xs text-red-600">{errors.password}</p> : null}
            </div>

            <div>
              <label
                htmlFor="confirmPassword"
                className="mb-1.5 block text-sm font-medium text-zinc-800"
              >
                Confirm Password
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                value={form.confirmPassword}
                onChange={event => setField("confirmPassword", event.target.value)}
                className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              />
              {errors.confirmPassword ? (
                <p className="mt-1.5 text-xs text-red-600">{errors.confirmPassword}</p>
              ) : null}
            </div>

            <button
              type="submit"
              className="mt-2 inline-flex h-11 w-full items-center justify-center rounded-xl bg-zinc-900 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
            >
              Register
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-zinc-600">
            Already have an account?{" "}
            <a href="/login" className="font-medium text-zinc-900 underline-offset-2 hover:underline">
              Login
            </a>
          </p>
        </section>
      </div>
    </main>
  );
}
