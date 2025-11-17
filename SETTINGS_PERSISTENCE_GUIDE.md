# Settings Persistence Guide - SharedPreferences

## Tổng Quan

Ứng dụng đã được cập nhật để lưu trữ các cài đặt người dùng bằng SharedPreferences với các quy tắc sau:

## Cài Đặt Được Lưu Trữ

### ✅ Temperature Units (Đơn vị nhiệt độ)
- **Lưu trữ**: ✅ **PERSISTENT** (lưu vĩnh viễn)
- **Key**: `temperature_unit`
- **Giá trị**: `celsius` hoặc `fahrenheit`
- **Mặc định**: `celsius` (°C)
- **Hành vi**: 
  - Lựa chọn được LƯU LẠI khi đóng app
  - Khi mở lại app → hiển thị lựa chọn trước đó
  - Hiển thị xác nhận khi thay đổi: "Temperature unit changed to..."

### ✅ Wind Speed Units (Đơn vị tốc độ gió)
- **Lưu trữ**: ✅ **PERSISTENT** (lưu vĩnh viễn)
- **Key**: `wind_speed_unit`
- **Giá trị**: `beaufort`, `kmh`, hoặc `ms`
- **Mặc định**: `beaufort` (Beaufort scale)
- **Hành vi**:
  - Lựa chọn được LƯU LẠI khi đóng app
  - Khi mở lại app → hiển thị lựa chọn trước đó
  - Hiển thị xác nhận khi thay đổi: "Wind speed unit changed to..."

### ❌ Update at Night Automatically (Cập nhật tự động ban đêm)
- **Lưu trữ**: ❌ **NOT PERSISTENT** (không lưu)
- **Key**: `night_update_enabled`
- **Giá trị**: `false` (luôn luôn)
- **Mặc định**: `false` (tắt)
- **Hành vi**:
  - **LUÔN LUÔN TẮT** mỗi khi mở app
  - Người dùng phải BẬT LẠI thủ công nếu muốn sử dụng
  - Thông báo: "Night update enabled for this session"
  - Alarm được hủy khi đóng/mở lại Settings

## Chi Tiết Kỹ Thuật

### File Lưu Trữ
```java
SharedPreferences file: "WeatherAppPrefs"
Mode: Context.MODE_PRIVATE
```

### Cấu Trúc Dữ Liệu
```
WeatherAppPrefs:
├── temperature_unit: "celsius" | "fahrenheit"
├── wind_speed_unit: "beaufort" | "kmh" | "ms"
└── night_update_enabled: false (always reset on open)
```

### Code Implementation

#### 1. Khởi Tạo SharedPreferences
```java
private static final String PREFS_NAME = "WeatherAppPrefs";
sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
```

#### 2. Load Settings (trong loadSavedSettings())
```java
// Temperature - LOAD từ storage
String tempUnit = sharedPreferences.getString(KEY_TEMP_UNIT, TEMP_CELSIUS);
updateTemperatureUnitDisplay(tempUnit);

// Wind Speed - LOAD từ storage
String windUnit = sharedPreferences.getString(KEY_WIND_UNIT, WIND_BEAUFORT);
updateWindSpeedUnitDisplay(windUnit);

// Night Update - ALWAYS DISABLE
switchNightUpdate.setChecked(false);
sharedPreferences.edit().putBoolean(KEY_NIGHT_UPDATE, false).apply();
cancelNightUpdate();
```

#### 3. Save Temperature Unit
```java
String selectedUnit = (which == 0) ? TEMP_CELSIUS : TEMP_FAHRENHEIT;
sharedPreferences.edit().putString(KEY_TEMP_UNIT, selectedUnit).apply();
updateTemperatureUnitDisplay(selectedUnit);
```

#### 4. Save Wind Speed Unit
```java
sharedPreferences.edit().putString(KEY_WIND_UNIT, selectedUnit).apply();
updateWindSpeedUnitDisplay(selectedUnit);
```

#### 5. Night Update (Không Lưu)
```java
// Không save vào SharedPreferences
// Chỉ schedule alarm cho session hiện tại
if (isChecked) {
    scheduleNightUpdate();
} else {
    cancelNightUpdate();
}
```

### Truy Xuất Settings Từ Các Activity Khác

```java
// Get Temperature Unit
String tempUnit = SettingsActivity.getTemperatureUnit(context);

// Get Wind Speed Unit
String windUnit = SettingsActivity.getWindSpeedUnit(context);

// Check Night Update (always false on app open)
boolean nightUpdate = SettingsActivity.isNightUpdateEnabled(context);
```

## Luồng Hoạt Động (Workflow)

### Lần Đầu Mở App
```
1. App khởi động
2. SharedPreferences được load
3. Không có dữ liệu → dùng giá trị mặc định:
   - Temperature: Celsius (°C)
   - Wind Speed: Beaufort scale
   - Night Update: OFF (disabled)
```

### Người Dùng Thay Đổi Settings
```
1. Mở Settings
2. Chọn Temperature unit → Lưu ngay vào SharedPreferences
3. Chọn Wind speed unit → Lưu ngay vào SharedPreferences
4. Bật Night Update → KHÔNG lưu, chỉ schedule alarm
5. Đóng Settings
```

### Mở Lại App
```
1. App khởi động lại
2. Load SharedPreferences:
   ✅ Temperature unit → Hiển thị lựa chọn trước đó
   ✅ Wind speed unit → Hiển thị lựa chọn trước đó
   ❌ Night Update → TỰ ĐỘNG TẮT (reset về false)
```

### Mở Lại Settings
```
1. Mở Settings Activity
2. loadSavedSettings() được gọi:
   ✅ Temperature: Load từ storage
   ✅ Wind Speed: Load từ storage
   ❌ Night Update: Force set to OFF
   ❌ Cancel all scheduled alarms
```

## User Experience

### Temperature & Wind Speed Units
- ✅ **Persistent**: Lựa chọn được nhớ mãi mãi
- ✅ **Convenient**: Chỉ cần chọn 1 lần
- ✅ **Immediate feedback**: Toast message xác nhận
- ✅ **Display updates**: UI cập nhật ngay lập tức

### Night Update
- ❌ **Non-persistent**: Phải bật lại mỗi lần mở app
- ❌ **Session-only**: Chỉ hoạt động trong phiên hiện tại
- ✅ **Safe**: Tránh app tự động cập nhật khi không mong muốn
- ✅ **Clear feedback**: "for this session" trong thông báo

## Testing Instructions

### Test 1: Temperature Unit Persistence
1. Mở Settings
2. Chọn Temperature unit → Fahrenheit
3. Đóng app hoàn toàn (force close)
4. Mở lại app → Mở Settings
5. ✅ **Expected**: Temperature unit vẫn là Fahrenheit

### Test 2: Wind Speed Unit Persistence
1. Mở Settings
2. Chọn Wind speed unit → km/h
3. Đóng app hoàn toàn
4. Mở lại app → Mở Settings
5. ✅ **Expected**: Wind speed unit vẫn là km/h

### Test 3: Night Update Reset
1. Mở Settings
2. Bật Night Update (toggle ON)
3. Đóng Settings (back button)
4. Mở lại Settings
5. ✅ **Expected**: Night Update đã TẮT (OFF)

### Test 4: Night Update After App Restart
1. Mở Settings
2. Bật Night Update
3. Đóng app hoàn toàn
4. Mở lại app → Mở Settings
5. ✅ **Expected**: Night Update đã TẮT (OFF)

### Test 5: All Settings Together
1. Mở Settings
2. Chọn Temperature → Fahrenheit
3. Chọn Wind Speed → m/s
4. Bật Night Update
5. Đóng app hoàn toàn
6. Mở lại app → Mở Settings
7. ✅ **Expected**:
   - Temperature: Fahrenheit ✅
   - Wind Speed: m/s ✅
   - Night Update: OFF ❌

## Advantages (Ưu điểm)

### Temperature & Wind Speed Persistent
- ✅ Tiện lợi cho người dùng
- ✅ Không phải chọn lại mỗi lần
- ✅ Trải nghiệm mượt mà

### Night Update Non-Persistent
- ✅ An toàn về pin
- ✅ Tránh cập nhật không mong muốn
- ✅ Người dùng chủ động kiểm soát
- ✅ Không tốn tài nguyên khi không cần

## File Modified
- ✅ `SettingsActivity.java` - Updated với comments rõ ràng

## Summary

**PERSISTENT (Lưu mãi mãi):**
- ✅ Temperature Units
- ✅ Wind Speed Units

**NON-PERSISTENT (Reset mỗi lần mở):**
- ❌ Night Update (always disabled on app start)

**Status**: ✅ Hoàn thành và hoạt động như yêu cầu!

