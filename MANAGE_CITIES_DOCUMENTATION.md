# Manage Cities Feature - Documentation

## Tổng quan (Overview)

Tính năng quản lý thành phố cho phép người dùng:
- ✅ Tìm kiếm và thêm thành phố mới
- ✅ Xem danh sách các thành phố đã lưu
- ✅ Xóa thành phố (trừ thành phố mặc định)
- ✅ Vuốt ngang để xem thời tiết các thành phố khác nhau
- ✅ 1 thành phố mặc định không thể xóa

## Tính năng đã triển khai

### 1. Manage Cities Activity

**Màn hình quản lý thành phố** (`activity_manage_cities.xml`):
- Thanh tìm kiếm với icon search
- Danh sách thành phố hiển thị dạng card
- Mỗi card hiển thị:
  - Tên thành phố
  - Thời tiết hiện tại
  - Nhiệt độ lớn
  - Icon pin cho thành phố mặc định
  - Nút xóa (chỉ hiện cho thành phố không phải mặc định)

**Chức năng tìm kiếm**:
- Gõ tên thành phố vào thanh tìm kiếm
- Nhấn Enter hoặc nút Search
- Nếu tìm thấy → Hiện dialog xác nhận thêm
- Nếu đã tồn tại → Thông báo "City already exists"
- Nếu không tìm thấy → Thông báo "City not found"

**Danh sách thành phố có sẵn**:
- Singapore
- Hanoi, Ho Chi Minh City, Da Nang (Vietnam)
- Bangkok (Thailand)
- Tokyo (Japan)
- Seoul (South Korea)
- Beijing, Shanghai, Hong Kong (China)
- Kuala Lumpur (Malaysia)
- Jakarta (Indonesia)
- Manila (Philippines)
- New York, Los Angeles (USA)
- London (UK)
- Paris (France)
- Sydney (Australia)
- Dubai (UAE)
- Mumbai (India)

### 2. ViewPager2 - Swipe Between Cities

**MainActivity với ViewPager2**:
- Vuốt sang trái/phải để xem thời tiết thành phố khác
- Tên thành phố ở top bar cập nhật khi đổi trang
- Vị trí hiện tại được lưu tự động
- Khi quay lại app, hiển thị thành phố đã xem trước đó

**WeatherPageFragment**:
- Mỗi thành phố có một fragment riêng
- Hiển thị đầy đủ thông tin thời tiết:
  - Nhiệt độ hiện tại
  - Tình trạng thời tiết
  - Dự báo 24 giờ (horizontal scroll)
  - 6 thẻ thông tin: UV, Humidity, Real Feel, Wind, Sunset, Pressure

### 3. City Manager - Data Storage

**CityManager.java**:
- Lưu trữ danh sách thành phố bằng SharedPreferences + Gson
- Quản lý thành phố mặc định (Binh Tan)
- Lưu vị trí thành phố đang xem
- Kiểm tra thành phố đã tồn tại
- Thêm/xóa thành phố

**City Model**:
- Tên thành phố
- Quốc gia
- Flag mặc định (isDefault)
- Tọa độ (latitude, longitude)

### 4. Navigation

**Từ Main Activity**:
- Nút `+` → Mở ManageCitiesActivity
- Nút `⋮` → Mở SettingsActivity

**Từ Manage Cities Activity**:
- Tap vào card thành phố → Quay về Main và hiển thị thành phố đó
- Nút xóa → Xóa thành phố (có xác nhận)
- Nút back → Quay về Main

## Cấu trúc File

### Java Classes:
```
com.example.weatherapp/
├── City.java                     // Model class
├── CityManager.java              // Storage & management
├── ManageCitiesActivity.java    // Search & manage cities
├── CityAdapter.java              // RecyclerView adapter
├── MainActivity.java             // Main screen with ViewPager2
├── WeatherPageFragment.java     // Weather page for each city
└── WeatherPagerAdapter.java     // ViewPager2 adapter
```

### Layouts:
```
res/layout/
├── activity_manage_cities.xml   // Manage cities screen
├── item_city.xml                // City card item
├── activity_main.xml            // Main screen with ViewPager2
└── fragment_weather_page.xml   // Weather fragment
```

## Sử dụng (Usage)

### Thêm thành phố mới:

1. Mở app → Tap nút `+` ở góc trên bên phải
2. Nhập tên thành phố (ví dụ: "Singapore")
3. Nhấn Enter
4. Tap "Add" trong dialog xác nhận
5. Thành phố được thêm vào danh sách

### Xem thời tiết thành phố khác:

**Cách 1: Vuốt ngang**
- Từ màn hình chính, vuốt sang trái hoặc phải
- Thời tiết thành phố khác sẽ hiện ra
- Tên thành phố ở top cập nhật tự động

**Cách 2: Chọn từ danh sách**
- Tap nút `+` → Mở Manage Cities
- Tap vào card thành phố muốn xem
- Quay về màn hình chính với thành phố đã chọn

### Xóa thành phố:

1. Tap nút `+` → Mở Manage Cities
2. Tap nút xóa (icon thùng rác) trên card
3. Xác nhận xóa trong dialog
4. Thành phố bị xóa khỏi danh sách

**Lưu ý**: Thành phố mặc định (có icon pin) không thể xóa!

## Kỹ thuật Implementation

### ViewPager2 Setup:
```java
// MainActivity.java
viewPagerWeather = findViewById(R.id.viewPagerWeather);
cities = cityManager.getCities();
pagerAdapter = new WeatherPagerAdapter(this, cities);
viewPagerWeather.setAdapter(pagerAdapter);

// Listen for page changes
viewPagerWeather.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
        updateLocationName(cities.get(position));
        cityManager.saveCurrentCityIndex(position);
    }
});
```

### City Storage with Gson:
```java
// Save cities
String json = gson.toJson(cities);
prefs.edit().putString(KEY_CITIES, json).apply();

// Load cities
String json = prefs.getString(KEY_CITIES, null);
Type type = new TypeToken<List<City>>(){}.getType();
List<City> cities = gson.fromJson(json, type);
```

### Search Implementation:
```java
private String[] findCity(String query) {
    String lowerQuery = query.toLowerCase();
    for (String[] city : WORLD_CITIES) {
        if (city[0].toLowerCase().contains(lowerQuery)) {
            return city; // [cityName, country]
        }
    }
    return null;
}
```

## Dependencies Added

```gradle
// build.gradle.kts
implementation(libs.gson)           // For JSON storage
implementation(libs.viewpager2)     // For swipeable pages
```

## UI Design

### Colors:
- Background: Black (#000000)
- City cards: Blue gradient (#4D88C6)
- Search bar: Dark gray (#333333)
- Text: White (#FFFFFF)
- Secondary text: Light gray (#CCFFFFFF)

### Layout:
- Top bar: 56dp height
- Search bar: 24dp corner radius
- City cards: 20dp corner radius
- Padding: 16dp standard
- Weather cards: Same as homepage (20dp radius)

## Features Summary

✅ **Completed:**
- Tìm kiếm thành phố từ database
- Thêm thành phố vào danh sách
- Xóa thành phố (trừ mặc định)
- Thành phố mặc định không thể xóa (có icon pin)
- Vuốt ngang để chuyển thành phố
- Lưu vị trí thành phố đang xem
- Tự động quay về thành phố đã xem
- 20 thành phố có sẵn để tìm kiếm

🔄 **Có thể mở rộng:**
- Thêm API thực để tìm kiếm toàn bộ thành phố thế giới
- Hiển thị thời tiết thực tế từ API
- Thêm vị trí GPS hiện tại
- Sắp xếp thứ tự thành phố (drag & drop)
- Thêm ảnh nền khác nhau cho mỗi thành phố

## Testing

**Kịch bản test:**
1. ✅ Thêm thành phố mới
2. ✅ Thêm thành phố đã tồn tại → Hiện thông báo
3. ✅ Tìm thành phố không tồn tại → Hiện thông báo
4. ✅ Xóa thành phố thường
5. ✅ Xóa thành phố mặc định → Không xóa được
6. ✅ Vuốt ngang giữa các thành phố
7. ✅ Tên thành phố cập nhật đúng
8. ✅ Vị trí được lưu khi thoát app
9. ✅ Quay lại đúng thành phố đã xem

## Notes

- Thành phố mặc định: **Binh Tan, Vietnam**
- Database thành phố: Hardcoded trong `ManageCitiesActivity.WORLD_CITIES`
- Có thể thêm nhiều thành phố hơn vào database
- ViewPager2 hỗ trợ swipe mượt mà
- Dữ liệu được lưu bằng SharedPreferences + Gson (persistent)

---

**Tất cả tính năng đã hoàn thành và sẵn sàng sử dụng!** 🎉

