Perfect 👌 thanks for sharing the screenshot.
I see you’re running your Spring Boot app in **IntelliJ IDEA** and currently looking at the **Run/Debug Configurations** window.

Here’s what you need to do:

---

### ✅ Add JVM Option (`-Duser.timezone=Asia/Kolkata`)

1. In the **Run/Debug Configurations** window (the one you showed).
2. At the top, click on **Modify options** (near top-right).
3. From the dropdown, select **Add VM options**.
   → A new **VM options** field will appear just below the "Program arguments".
4. In that **VM options** field, paste:

```
-Duser.timezone=Asia/Kolkata
```

5. Click **Apply** → **OK**.
6. Now **Run** your application again.

---

### ⚡ What This Does

This sets the **JVM default timezone** to `Asia/Kolkata`, so Hibernate and JDBC will use it when connecting to PostgreSQL.
This avoids the `FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"` error.

---

👉 Do you want me to also show you how to **verify inside the app** (e.g., log the JVM timezone at startup) so you can confirm it’s correctly applied?
