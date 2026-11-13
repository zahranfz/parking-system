# Aplikasi Sistem Parkir Sederhana (Studi Kasus OOP Java)

Aplikasi desktop berbasis Java Swing untuk pengelolaan operasional parkir kendaraan (Check-In, Check-Out, Monitoring Slot Real-Time, dan Riwayat Transaksi). Proyek ini dirancang secara khusus untuk mendemonstrasikan implementasi 6 konsep fundamental *Object-Oriented Programming* (OOP) dalam satu aplikasi utuh tanpa menggunakan `enum`.

---

## 👥 Identitas Kelompok

* **Kelompok:** Kelompok 3  
* **Nama Kelompok:** Sistem Parkir  
* **Mata Kuliah:** Pemrograman Berorientasi Objek (PBO)  

### Anggota Kelompok
| No | Nama Mahasiswa | NIM | Peran / Kontribusi |
|:--:|---|:--:|---|
| 1 | Zahran Febrian Nugraha | H1D025074 | Service Layer & Business Logic Orchestration |
| 2 | Muhammad Nabil Adly Wirman | H1D025076 | Model Hierarki Kendaraan & OOP Contracts |
| 3 | Agung Barkah Syaputra | H1D025080 | Repository Layer & CSV Persistence Handler |
| 4 | Dhandy Putra Fahruddin | H1D025097 | UI Development (Swing Panels & MainFrame) |
| 5 | Sadewa Nur Wahyudi | H1D025112 | Unit Testing, Validasi, & Dokumentasi PRD/README |

---

## 📌 Penjelasan Program & Penerapan Konsep OOP

Aplikasi ini mengelola alur kendaraan masuk dan keluar pada area parkir bertarif per jam dengan kuota kapasitas terbatas. Seluruh arsitektur kode dibangun menggunakan kaidah OOP modular:

| Konsep OOP | Implementasi Nyata dalam Kode |
|---|---|
| **Class & Object** | Objek nyata dimodelkan menjadi class seperti `Vehicle`, `ParkingSlot`, `Transaction`, dan `ParkingLot`. |
| **Encapsulation** | Semua atribut entitas dideklarasikan `private`/`protected` dengan akses melalui getter dan method operasional, dilengkapi validasi input (plat nomor tidak boleh kosong, kuota tidak negatif, pencegahan plat duplikat). |
| **Inheritance** | Class `Motorcycle`, `Car`, dan `Truck` mewarisi properti dasar (`licensePlate`, `entryTime`) dari abstract class `Vehicle`. |
| **Abstract Class** | `Vehicle` berperan sebagai abstract base class yang mendefinisikan method abstrak `getRatePerHour()` dan `getVehicleType()`. |
| **Interface** | - `CsvWritable`: Kontrak lintas layer bagi objek yang dapat diubah ke format baris CSV (`toCsvRow()`).<br>- `TransactionRepository`: Abstraksi media penyimpanan untuk memisahkan logic bisnis dari persistensi fisik. |
| **Polymorphism** | - *Dynamic Method Dispatch*: Perhitungan biaya parkir memanggil `getRatePerHour()` secara polimorfik sesuai subclass kendaraan nyata.<br>- *Interface Polymorphism*: `ParkingService` berinteraksi dengan `TransactionRepository` tanpa bergantung langsung pada implementasi file CSV. |
| **Guardrail Materi** | **Bebas dari `enum`** — jenis kendaraan direpresentasikan melalui hierarki inheritance class dan collection terstruktur. |

---

## Aturan Bisnis (Business Rules)

1. **Jenis Kendaraan & Tarif (BR-01 & BR-03):**
   - **Motor (`Motorcycle`):** Rp2.000 / jam
   - **Mobil (`Car`):** Rp5.000 / jam
   - **Truk (`Truck`):** Rp10.000 / jam
2. **Kuota Kapasitas Slot (BR-02):**
   - Motor: 10 slot (`MTR-01` s/d `MTR-10`)
   - Mobil: 5 slot (`MBL-01` s/d `MBL-05`)
   - Truk: 2 slot (`TRK-01` s/d `TRK-02`)
3. **Perhitungan Durasi & Biaya (BR-04 & BR-05):**
   - Durasi dibulatkan ke atas per jam mulai (contoh: parkir 1 jam 5 menit dihitung 2 jam).
   - Minimal durasi penagihan adalah 1 jam, meskipun parkir kurang dari 1 jam.
4. **Validasi Check-In (BR-06 & BR-07):**
   - Check-in ditolak jika kuota slot jenis kendaraan bersangkutan sedang penuh.
   - Satu plat nomor tidak dapat check-in dua kali selama belum melakukan check-out.
5. **Validasi Check-Out (BR-08):**
   - Check-out hanya dapat dilakukan untuk kendaraan yang saat ini tercatat sedang parkir.

---

## Alur Program (Workflow)

```
[ Operator Parkir ]
       │
       ├─► [Tab 1: Check-In] ────► Input Plat + Pilih Jenis ──► Slot Tersedia? ──► [Alokasikan Slot & Catat Waktu]
       │                                                              │
       │                                                              └─► Penuh? ─► [Tampilkan Pesan Tolak]
       │
       ├─► [Tab 2: Check-Out] ───► Input Plat Nomor ──────────► Ada di Slot? ──► [Hitung Durasi & Biaya]
       │                                                              │                │
       │                                                              │                ▼
       │                                                              │        [Simpan ke transactions.csv]
       │                                                              │                │
       │                                                              │                ▼
       │                                                              │        [Bebaskan Slot Parkir]
       │                                                              │
       │                                                              └─► Tidak Ada ─► [Kendaraan Tidak Ditemukan]
       │
       ├─► [Tab 3: Status Slot] ─► Tampilan Real-Time Sisa Kapasitas & Daftar Detail Slot Parkir
       │
       └─► [Tab 4: Riwayat] ─────► Menampilkan Riwayat Transaksi Selesai Langsung dari File CSV
```

### 1. Check-In Kendaraan
- Operator memasukkan nomor plat dan memilih jenis kendaraan melalui dropdown (`JComboBox`).
- Sistem memvalidasi ketersediaan slot dan memeriksa apakah plat sudah terdaftar aktif.
- Jika lolos, kendaraan ditempatkan ke slot kosong yang sesuai, waktu masuk dicatat, dan konfirmasi ditampilkan.

### 2. Check-Out Kendaraan
- Operator memasukkan nomor plat kendaraan yang akan keluar.
- Sistem mencari slot aktif kendaraan tersebut, menghitung selisih waktu masuk dan keluar, serta membulatkan durasi ke atas per jam.
- Biaya dihitung berdasarkan tarif per jam dari objek kendaraan yang bersangkutan.
- Slot parkir dibebaskan dan data transaksi otomatis disimpan ke file `transactions.csv`.

### 3. Monitoring Status Slot
- Menampilkan ringkasan sisa kapasitas (Total, Terisi, Tersedia) per jenis kendaraan secara real-time.
- Menampilkan tabel detail untuk setiap slot (`ID Slot`, `Jenis`, `Status`, `Plat Nomor`, `Waktu Masuk`).

### 4. Riwayat Transaksi
- Menampilkan tabel riwayat transaksi selesai yang dibaca langsung dari file `transactions.csv`.
- Dilengkapi tombol **Muat Ulang** untuk menyinkronkan data terbaru.
- Parser CSV memiliki mitigasi otomatis untuk melewati baris yang rusak/tidak valid tanpa menyebabkan aplikasi berhenti (*crash*).

---

## Teknologi yang Digunakan

* **Bahasa Pemrograman:** Java (JDK 21 LTS)
* **User Interface:** Java Swing (Desktop GUI berbasis `JFrame`, `JTabbedPane`, `JTable`)
* **Build Tool & Dependency Management:** Apache Maven 3.9+
* **Unit Testing:** JUnit 5 (JUnit Jupiter 5.10.2)
* **Penyimpanan Data (Persistence):** Local Flat File (`transactions.csv`) dengan encoding UTF-8

---

## Struktur Direktori Proyek

```text
parking-system/
├── pom.xml                               # Konfigurasi Maven dan dependensi JUnit 5
├── transactions.csv                      # File data riwayat transaksi parkir
├── README.md                             # Dokumentasi proyek
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── parkingsystem/
    │               ├── Main.java         # Entry point aplikasi (Swing bootstrap)
    │               ├── common/
    │               │   └── CsvWritable.java              # Interface kontrak baris CSV
    │               ├── model/
    │               │   ├── Vehicle.java                  # Abstract class induk kendaraan
    │               │   ├── Motorcycle.java               # Subclass kendaraan Motor
    │               │   ├── Car.java                      # Subclass kendaraan Mobil
    │               │   ├── Truck.java                    # Subclass kendaraan Truk
    │               │   ├── ParkingSlot.java              # Class status slot parkir
    │               │   └── Transaction.java              # Class catatan transaksi selesai
    │               ├── repository/
    │               │   ├── TransactionRepository.java    # Interface abstraksi persistensi
    │               │   └── CsvTransactionRepository.java # Implementasi baca/tulis CSV
    │               ├── service/
    │               │   ├── ParkingLot.java               # Pengelola kumpulan slot parkir
    │               │   └── ParkingService.java           # Orkestrator alur bisnis parkir
    │               └── ui/
    │                   ├── MainFrame.java                # Window utama (JTabbedPane)
    │                   ├── CheckInPanel.java             # Panel form check-in
    │                   ├── CheckOutPanel.java            # Panel form check-out & tarif
    │                   ├── SlotStatusPanel.java          # Panel status slot real-time
    │                   └── HistoryPanel.java             # Panel tabel riwayat dari CSV
    └── test/
        └── java/
            └── com/
                └── parkingsystem/
                    ├── repository/
                    │   └── CsvTransactionRepositoryTest.java # Unit test persistensi CSV
                    ├── service/
                    │   └── ParkingServiceTest.java           # Unit test aturan bisnis parkir
                    └── ui/
                        └── MainFrameTest.java                # UI smoke test inisialisasi tab
```

---

## Panduan Menjalankan Aplikasi

### 1. Prasyarat Sistem
- Java Development Kit (JDK) 21 atau lebih baru terpasang di sistem (`java -version`).
- Apache Maven 3.8+ terpasang di sistem (`mvn -version`).

### 2. Kompilasi & Build Proyek
Jalankan perintah berikut di root direktori proyek:
```bash
mvn clean package
```
Perintah ini akan mengompilasi seluruh source code, mengeksekusi seluruh pengujian unit test, dan menghasilkan file executable JAR di `target/parking-system-1.0.0.jar`.

### 3. Menjalankan Aplikasi
Aplikasi dapat dijalankan melalui salah satu cara berikut:

**Opsi A — Menggunakan Maven Exec Plugin:**
```bash
mvn exec:java
```

**Opsi B — Menjalankan File JAR yang Telah Di-build:**
```bash
java -jar target/parking-system-1.0.0.jar
```

### 4. Menjalankan Unit Test
Untuk memverifikasi kebenaran business rules dan parser CSV:
```bash
mvn test
```
Seluruh 11 pengujian otomatis akan dijalankan mencakup:
- Validasi kuota default slot.
- Perhitungan tarif minimum 1 jam.
- Pembulatan ke atas per jam mulai.
- Penolakan check-in saat slot penuh.
- Penolakan check-in duplikat untuk plat yang sama.
- Penolakan check-out kendaraan yang tidak ditemukan.
- Round-trip penyimpanan dan pembacaan CSV.
- Penanganan baris CSV yang korup secara aman.
- Inisialisasi komponen GUI 4 tab pada `MainFrame`.

---

## Format File Penyimpanan (`transactions.csv`)

File `transactions.csv` disimpan di direktori kerja aplikasi dengan struktur baris:
```csv
plat_nomor,jenis_kendaraan,waktu_masuk,waktu_keluar,durasi_jam,biaya
B 1234 ABC,Motor,2026-11-12 10:00:00,2026-11-12 11:30:00,2,4000
D 5678 XYZ,Mobil,2026-11-12 10:15:00,2026-11-12 11:00:00,1,5000
```
Jika file belum ada, sistem akan otomatis membuatnya beserta header kolom saat transaksi pertama selesai dilakukan.

