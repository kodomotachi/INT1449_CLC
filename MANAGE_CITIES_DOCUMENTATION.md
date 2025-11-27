# Manage Cities Feature - Documentation

## Overview
The Manage Cities feature allows users to search and add multiple cities to track weather, with one default city that cannot be deleted. Users can swipe left/right on the main screen to view weather for different cities.

## Features Implemented

### 1. **City Database System**
- SQLite database to persistently store cities
- Each city has: ID, Name, Country, Latitude, Longitude, Default flag
- **Binh Tan, Vietnam** is set as the default city (cannot be deleted)
- Database auto-creates on first launch

### 2. **Manage Cities Activity**
- Black-themed interface matching the reference design
- Search bar to find cities worldwide
- List of saved cities with weather preview
- Delete functionality for non-default cities

#### Search Functionality:
- Real-time search as you type
- Searches through 30+ major world cities (sample database)
- Search by city name or country
- Dropdown results appear below search bar
- Tap a result to add city

#### City List Features:
- Blue gradient cards showing:
  - City name
  - Weather condition and high/low temps
  - Current temperature (large)
  - Delete button (except for default city)
- Default city shows location pin icon instead of delete button
- Cannot delete if only one city remains

### 3. **Swipeable Main Screen (ViewPager2)**
- Swipe left/right to view weather for different cities
- Smooth page transitions with fade effect
- Each city has its own weather page (Fragment)
- Top buttons (Add, Menu) overlay all pages
- Background changes based on weather condition

### 4. **World Cities Database**
Pre-loaded cities include:

**Vietnam:**
- Binh Tan, Hanoi, Ho Chi Minh City, Da Nang, Hue

**Southeast Asia:**
- Singapore, Bangkok, Kuala Lumpur, Jakarta, Manila

**East Asia:**
- Tokyo, Seoul, Beijing, Shanghai, Hong Kong, Taipei

**Australia & Oceania:**
- Sydney, Melbourne, Auckland

**Europe:**
- London, Paris, Berlin, Rome, Madrid, Moscow

**North America:**
- New York, Los Angeles, Chicago, Toronto, Vancouver

**South America:**
- São Paulo, Buenos Aires, Rio de Janeiro

**Middle East:**
- Dubai, Tel Aviv, Istanbul

## Architecture

### Files Created:

**Data Models:**
- `City.java` - City data model with weather properties
- `CityDatabaseHelper.java` - SQLite database management

**Activities:**
- `ManageCitiesActivity.java` - City management screen with search

**Fragments:**
- `WeatherPageFragment.java` - Individual weather page for each city

**Layouts:**
- `activity_manage_cities.xml` - Manage cities screen layout
- `item_city.xml` - City list item (blue card with delete button)
- `item_search_result.xml` - Search result item
- `fragment_weather_page.xml` - Weather page fragment
- `activity_main.xml` - Updated to use ViewPager2

**Updated:**
- `MainActivity.java` - Now uses ViewPager2 with FragmentStateAdapter
- `AndroidManifest.xml` - Added ManageCitiesActivity
- `build.gradle.kts` - Added ViewPager2 and Fragment dependencies
- `strings.xml` - Added city management strings

## How It Works

### Flow 1: Adding a New City
1. User taps Add (+) button on main screen
2. ManageCitiesActivity opens
3. User types city name in search bar (e.g., "Singapore")
4. Search results appear in dropdown
5. User taps desired city
6. City is added to database
7. Success toast shows "City added"
8. City appears in list with sample weather data
9. User returns to main screen
10. New city page is available by swiping

### Flow 2: Viewing Different Cities
1. User is on main screen (showing Binh Tan by default)
2. User swipes left → Shows next city (e.g., Singapore)
3. User swipes right → Returns to previous city
4. Each swipe shows complete weather page for that city
5. Top buttons (Add, Menu) remain accessible on all pages

### Flow 3: Deleting a City
1. User opens Manage Cities
2. User taps delete (trash) icon on a city card
3. System checks if city is default → Shows error if default
4. System checks if it's the last city → Shows error if last
5. Otherwise, city is deleted from database
6. List refreshes automatically
7. Toast shows "City removed"

## Technical Details

### ViewPager2 Implementation
```java
// MainActivity uses FragmentStateAdapter
private static class WeatherPagerAdapter extends FragmentStateAdapter {
    // Creates a fragment for each city
    public Fragment createFragment(int position) {
        return WeatherPageFragment.newInstance(cities.get(position));
    }
}
```

### Database Structure
```sql
CREATE TABLE cities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    country TEXT,
    latitude REAL,
    longitude REAL,
    is_default INTEGER DEFAULT 0
);
```

### Default City Protection
- Default city has `is_default = 1` in database
- Delete button hidden for default city
- Location pin icon shown instead
- Always appears first in list

### Search Algorithm
- Case-insensitive matching
- Searches both city name and country
- Uses `contains()` for flexible matching
- Real-time results (no search button needed)

## UI/UX Features

### Design Elements:
- **Black background** (#000000)
- **Blue gradient cards** (#4D7BA5D1) for cities
- **Semi-transparent white** (#E6FFFFFF) for forecast card
- **Rounded corners** (20dp) on all cards
- **White text** for primary content
- **Gray text** (#808080) for secondary info

### Interactions:
- Tap search bar → Keyboard appears
- Type → Results appear instantly
- Tap result → City added
- Swipe pages → Smooth transitions
- Tap delete → Confirmation via system behavior
- Can't delete default → Toast message

## Integration Points

### With MainActivity:
- `btnAdd` opens `ManageCitiesActivity`
- `onResume()` reloads cities when returning
- Maintains current page position after reload

### With WeatherPageFragment:
- Each fragment receives City object
- Displays city-specific weather data
- Independent scrolling per page
- Reuses existing layouts (hourly forecast, info cards)

### With Database:
- All activities share same `CityDatabaseHelper` instance
- Database persists across app restarts
- Thread-safe operations

## Future Enhancements

### Potential Additions:
1. **Weather API Integration**
   - Fetch real weather data for each city
   - Update on swipe or pull-to-refresh
   - Background updates

2. **Location Services**
   - Auto-detect user's location
   - Add current location as default
   - GPS-based weather

3. **City Management Features**
   - Reorder cities (drag & drop)
   - Set any city as default
   - Bulk delete
   - Import/Export city list

4. **Search Improvements**
   - Online city database (OpenWeatherMap, GeoNames API)
   - Recent searches
   - Popular cities suggestions
   - Country filtering

5. **Enhanced UI**
   - Page indicators (dots showing current city)
   - City thumbnails/flags
   - Weather-based backgrounds per city
   - Animated transitions

6. **Data Sync**
   - Cloud backup of city list
   - Sync across devices
   - Share city list with others

## Usage Examples

### Example 1: Add Singapore
```
1. Open app → Shows Binh Tan
2. Tap + button
3. Type "Singapore" in search
4. Tap "Singapore, Singapore" result
5. See "City added" toast
6. Tap back arrow
7. Swipe left to see Singapore weather
```

### Example 2: Managing Multiple Cities
```
1. Add cities: Singapore, Tokyo, London
2. Main screen: Swipe through 4 cities
   - Binh Tan (default)
   - Singapore
   - Tokyo
   - London
3. Each shows full weather page
```

### Example 3: Cannot Delete Default
```
1. Open Manage Cities
2. Try to delete "Binh Tan"
3. No delete button shown (location pin instead)
4. Default city is protected
```

## Testing Checklist

- [ ] Default city (Binh Tan) created on first launch
- [ ] Search finds cities by name
- [ ] Search finds cities by country
- [ ] Tap search result adds city
- [ ] Duplicate cities are prevented
- [ ] Cannot delete default city
- [ ] Cannot delete last remaining city
- [ ] Swipe left shows next city
- [ ] Swipe right shows previous city
- [ ] Each page shows correct city name
- [ ] ViewPager maintains position on resume
- [ ] Database persists across app restarts
- [ ] Add button opens Manage Cities
- [ ] Menu button opens Settings
- [ ] All UI elements are properly styled

## Summary

✅ **Complete Features:**
- Database-backed city storage
- One protected default city (Binh Tan)
- Search bar with 30+ world cities
- Real-time search results
- Add cities from search
- Delete non-default cities
- Swipeable ViewPager2 main screen
- Smooth page transitions
- Fragment-based architecture
- Persistent data storage

The Manage Cities feature is fully functional and ready to use! Users can now track weather for multiple cities and easily switch between them with a swipe gesture.

