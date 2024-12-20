object DataDummy {
    fun generateDummyStories(): List<ListStoryItem> {
        return List(10) { index ->
            ListStoryItem(
                id = index.toString(),
                name = "Story $index",
                description = "Description $index",  // atau bisa null jika tidak ingin ada deskripsi
                photoUrl = "https://example.com/photo_$index.jpg",
                lat = null,  // Bisa diberikan null jika tidak ada informasi koordinat
                lon = null   // Bisa diberikan null jika tidak ada informasi koordinat
            )
        }
    }
}
