# 📦 Dokumentasi Struktur Proyek Microservice E-Commerce

Dokumentasi ini disusun untuk membantu pengembang memahami struktur global proyek microservices berbasis Java Spring Boot. Contoh yang digunakan adalah dari `transaction-service`, namun struktur dan praktik yang digunakan berlaku untuk seluruh service.

---

## 🧭 Struktur Direktori Global

```
Tugas Rancang/
├── auth-service/
├── product-service/
├── order-service/
├── transaction-service/
├── api-gateway/
└── eureka-server
```

Setiap folder service memiliki struktur mirip dan dapat dikembangkan secara modular.

---

## 📁 Struktur Standar per Service

```
transaction-service/
├── src/main/java/com/ecommerce/transaction_service/
│   ├── Config/
│   │   └── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── Controller/
│   ├── DTO/
│   ├── Mapper/
│   ├── Middleware/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtMiddleware.java
│   │   ├── LoggingInterceptor.java
│   │   ├── UserContext.java
│   │   └── UserContextInterceptor.java
│   ├── Model/
│   ├── Repository/
│   ├── Service/
│   └── Utils/
│       ├── ApiResponse.java
│       └── GlobalExceptionHandler.java
└── resources/
```

---

## 🔐 Security & Config

### SecurityConfig.java

* Mengatur filter JWT dan keamanan endpoint.
* Menggunakan `HttpSecurity` dari Spring Security.

📥 Import:

```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.ecommerce.transaction_service.Middleware.JwtAuthenticationFilter;
```

### WebConfig.java

* Menggunakan Interceptor `UserContextInterceptor` untuk menyisipkan informasi user di setiap request.

📥 Import:

```java
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.ecommerce.transaction_service.Middleware.UserContextInterceptor;
```

---

## 🔄 Middleware

### JwtAuthenticationFilter.java

* Memvalidasi token JWT dan mengatur autentikasi pada context.

### JwtMiddleware.java

* Middleware manual untuk inject user-role, hanya jika tidak menggunakan filter.

### LoggingInterceptor.java

* Logging request dan response (opsional).

### UserContext.java

* Menyimpan data user saat ini (username, role, dsb) menggunakan `ThreadLocal`.

### UserContextInterceptor.java

* Menyisipkan `UserContext` berdasarkan header dari JWT setelah decode.

📥 Import umum:

```java
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
```

---

## ✅ Utils

### ApiResponse.java

* Template standar respons:

```json
{
  "message": "Berhasil",
  "status": 200,
  "data": {...}
}
```

📥 Import:

```java
import lombok.AllArgsConstructor;
import lombok.Data;
```

### GlobalExceptionHandler.java

* Menangani error `@Valid`, `RuntimeException`, dan lainnya.
* Memberikan pesan yang rapi untuk frontend.

📥 Import:

```java
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;
```

---

## 🧱 Panduan Membuat Komponen Baru

### 1. 📦 Service

```java
@Service
public class TransaksiService {
    // logika bisnis
}
```

Import:

```java
import org.springframework.stereotype.Service;
```

### 2. 🧭 Controller

```java
@RestController
@RequestMapping("/transaksi")
public class TransaksiController {
    // endpoint
}
```

Import:

```java
import org.springframework.web.bind.annotation.*;
```

### 3. 🗂️ Model

```java
@Entity
public class Transaksi {
    @Id
    private String id;
}
```

Import:

```java
import jakarta.persistence.*;
```

### 4. 📂 Repository

```java
public interface TransaksiRepository extends JpaRepository<Transaksi, String> {}
```

Import:

```java
import org.springframework.data.jpa.repository.JpaRepository;
```

### 5. 🧾 DTO

```java
@Data
public class TransaksiRequestDTO {
    private String nama;
}
```

Import:

```java
import lombok.Data;
```

### 6. 🔁 Mapper

```java
public class TransaksiMapper {
    public static Transaksi toEntity(TransaksiRequestDTO dto) {}
}
```

Import:

```java
// custom
```

---
