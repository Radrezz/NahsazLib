# Changelog - NahsazLib Library Management System

## [Update] 2026-02-08 - Jam Operasional & UI Improvements

### ✨ Fitur Baru

#### 1. Sistem Jam Operasional
- **Pembatasan Login Berdasarkan Jam Operasional**
  - Petugas dan Anggota hanya dapat login saat perpustakaan buka
  - Admin dapat login kapan saja (24/7)
  - Pesan error informatif saat perpustakaan tutup
  
- **Method Baru di DB.java**
  - `isLibraryOpen()` - Mengecek status operasional perpustakaan
  - `getLibraryStatusMessage()` - Mendapatkan pesan status jam operasional
  
- **Validasi Real-time**
  - Sistem otomatis mengecek hari dan jam saat login
  - Membandingkan dengan data operational_hours di database
  - Menampilkan jam buka/tutup untuk hari ini

#### 2. Input Kelas dengan Combo Box
- **RegisterPage.java**
  - Mengubah input kelas dari text field menjadi dropdown
  - Pilihan kelas: VII-A sampai IX-E (15 pilihan)
  - Format: "VII-A", "VII-B", ..., "IX-E"
  - Validasi otomatis, mencegah input salah
  
- **AnggotaPanel.java (Admin)**
  - Form edit anggota juga menggunakan combo box
  - Konsistensi data kelas di seluruh sistem
  - Auto-select kelas saat edit data

### 🎨 Perbaikan UI

#### 1. Layout Settings Panel
- **Rasio Layout Baru**
  - Application Logo: 20% tinggi (1 bagian)
  - Operational Hours: 80% tinggi (4 bagian)
  - Rasio 1:4 untuk kolom kiri
  
- **Struktur Grid**
  - 2 kolom utama dengan nested panels
  - Kolom kiri: Logo + Operational Hours
  - Kolom kanan: General Settings + Tools
  - Spacing konsisten: 16px antar kolom, 8px antar section

#### 2. Operational Hours Section
- **Layout GridBagLayout**
  - Semua elemen sejajar sempurna dalam kolom
  - Ukuran tetap untuk konsistensi:
    - Label hari: 90px
    - Checkbox "BUKA": 70px
    - Time spinner: 90px (start & end)
    - Separator "-": 20px
  
- **Perbaikan Visual**
  - Tinggi baris: 36px (konsisten untuk 7 hari)
  - Spacing horizontal: 10px antar elemen
  - Font size: 12pt (lebih mudah dibaca)
  - Semua 7 hari terlihat tanpa scroll (Minggu - Sabtu)

#### 3. Logo Section
- **Optimasi Ukuran**
  - Logo preview: 80x80px (dari 100x100px)
  - Circular image: 70px diameter
  - Spacing dikurangi untuk efisiensi ruang
  - Tetap terlihat jelas dan profesional

### 🔧 Perbaikan Teknis

#### 1. Database
- **Tabel operational_hours**
  - Auto-create saat aplikasi start
  - Default: Minggu tutup, Senin-Sabtu buka 08:00-17:00
  - Index 0-6 (Minggu-Sabtu)
  
#### 2. Login System
- **LoginPage.java**
  - Pengecekan role sebelum validasi jam operasional
  - Pesan error yang informatif dan user-friendly
  - Audit log tetap tercatat untuk semua attempt

#### 3. Data Consistency
- **Format Kelas**
  - Standarisasi format: "VII-A", "VIII-B", dll
  - Validasi di level UI (combo box)
  - Konsisten di registrasi dan edit data

### 📝 File yang Dimodifikasi

1. **src/nahlib/DB.java**
   - Added: `isLibraryOpen()` method
   - Added: `getLibraryStatusMessage()` method
   - Enhanced: `initOperationalHours()` method

2. **src/nahlib/LoginPage.java**
   - Added: Operational hours check in `doLogin()`
   - Enhanced: Error messaging for closed library

3. **src/nahlib/RegisterPage.java**
   - Changed: `tfKelas` (JTextField) → `cbKelas` (JComboBox)
   - Added: Class options population (VII-A to IX-E)
   - Updated: `doRegister()` to use combo box value

4. **src/nahlib/admin/AnggotaPanel.java**
   - Changed: `kelas` field from JTextField to JComboBox
   - Added: Class options in edit form
   - Updated: `saveAnggota()` method

5. **src/nahlib/admin/SettingsPanel.java**
   - Restructured: Main content layout (nested panels)
   - Changed: DayRow from FlowLayout to GridBagLayout
   - Fixed: Operational hours alignment
   - Optimized: Logo section size
   - Updated: Grid weights for 1:4 ratio

### 🎯 Manfaat Perubahan

#### Untuk Admin
- ✅ Kontrol penuh atas jam operasional perpustakaan
- ✅ Akses sistem 24/7 untuk maintenance
- ✅ UI settings yang lebih rapi dan mudah digunakan

#### Untuk Petugas
- ✅ Login hanya saat jam kerja (sesuai operasional)
- ✅ Pesan jelas jika perpustakaan tutup
- ✅ Data kelas anggota lebih konsisten

#### Untuk Anggota/User
- ✅ Login hanya saat perpustakaan buka
- ✅ Registrasi lebih mudah dengan dropdown kelas
- ✅ Tidak ada kesalahan input kelas

#### Untuk Sistem
- ✅ Data kelas terstandarisasi
- ✅ Validasi otomatis di level UI
- ✅ Konsistensi data di database
- ✅ Audit trail tetap lengkap

---

## Cara Menggunakan Fitur Baru

### 1. Mengatur Jam Operasional (Admin)
1. Login sebagai Admin
2. Buka menu "Settings"
3. Scroll ke section "Operational Hours"
4. Untuk setiap hari:
   - Centang "BUKA" jika perpustakaan buka
   - Set jam buka (start time)
   - Set jam tutup (end time)
5. Klik "Save Changes"

### 2. Registrasi dengan Kelas Baru
1. Buka halaman registrasi
2. Isi data user
3. Pilih kelas dari dropdown (VII-A sampai IX-E)
4. Submit form

### 3. Edit Kelas Anggota (Admin)
1. Login sebagai Admin
2. Buka menu "Data Member"
3. Pilih anggota yang ingin diedit
4. Klik "Edit"
5. Pilih kelas baru dari dropdown
6. Klik "Update"

---

## Technical Notes

### Database Schema
```sql
-- Tabel operational_hours
CREATE TABLE operational_hours (
    day_index INT PRIMARY KEY,      -- 0=Minggu, 1=Senin, ..., 6=Sabtu
    day_name VARCHAR(10),            -- Nama hari
    is_open TINYINT DEFAULT 1,       -- 1=buka, 0=tutup
    open_time TIME DEFAULT '08:00:00',
    close_time TIME DEFAULT '17:00:00'
);
```

### Class Format
```
VII-A, VII-B, VII-C, VII-D, VII-E
VIII-A, VIII-B, VIII-C, VIII-D, VIII-E
IX-A, IX-B, IX-C, IX-D, IX-E
```

### Login Logic
```java
if (role != "ADMIN") {
    if (!DB.isLibraryOpen()) {
        // Block login
        // Show error message
        return;
    }
}
// Proceed with login
```

---

**Version**: 2.1
**Date**: 2026-02-08
**Author**: Development Team
