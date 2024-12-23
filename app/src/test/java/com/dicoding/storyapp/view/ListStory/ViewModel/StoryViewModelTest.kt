import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.getOrAwaitValue
import com.dicoding.storyapp.view.ListStory.StoryRepository
import com.dicoding.storyapp.view.ListStory.ViewModel.StoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class StoryViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var storyRepository: StoryRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        // Mocking dependencies
        storyRepository = mock(StoryRepository::class.java)
        userRepository = mock(UserRepository::class.java)

        // Mock Application context if necessary
        val context = mock(Application::class.java)

        // Memastikan StoryViewModel diinisialisasi dengan konteks yang benar dan dependency yang sudah dimock
        storyViewModel = StoryViewModel(context, storyRepository, userRepository)
    }

    @Test
    fun `when Get Stories Should Not Null and Return Data`() = runTest {
        // Generate dummy stories
        val dummyStories = DataDummy.generateDummyStories()

        // Ensure dummy data is not empty
        assertTrue("Dummy stories should not be empty", dummyStories.isNotEmpty())

        val dummyPagingData = PagingData.from(dummyStories)
        val expectedStoriesFlow = flowOf(dummyPagingData)

        Mockito.`when`(storyRepository.getStoriesPaged(Mockito.anyString()))
            .thenReturn(expectedStoriesFlow)

        val token = "dummy_token"
        Mockito.`when`(userRepository.getUserToken()).thenReturn(token)

        // Act
        val actualStories = storyViewModel.getPagedStories().asLiveData().getOrAwaitValue()

        // Use AsyncPagingDataDiffer to check item count
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = Dispatchers.Main,
        )

        differ.submitData(actualStories)

        // Assert
        assertNotNull("Actual stories should not be null", actualStories)

        // Ensure data is not empty
        assertTrue("Data should not be empty", differ.snapshot().size > 0)

        // Ensure the number of data matches
        assertEquals("Data count should match the expected size", dummyStories.size, differ.snapshot().size)

        // Ensure the first data matches
        val firstStory = dummyStories.first()
        val firstDifferStory = differ.snapshot().first()
        assertEquals("First story should match the expected story", firstStory, firstDifferStory)
    }


    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.empty() // Simulate empty PagingData
        val expectedStories = flowOf(data)

        // Make sure that StoryViewModel is initialized properly
        storyViewModel = StoryViewModel(mock(Application::class.java), storyRepository, userRepository)

        Mockito.`when`(storyRepository.getStoriesPaged(Mockito.anyString()))
            .thenReturn(expectedStories)

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = Dispatchers.Main,
        )

        val job = launch {
            storyViewModel.getPagedStories().collect { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle() // Simulate the flow to advance until idle
        job.cancel()

        assertEquals(0, differ.snapshot().size) // Assert empty data
    }

    // DiffCallback for ListStoryItem comparison
    class StoryDiffCallback : DiffUtil.ItemCallback<ListStoryItem>() {
        override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
            return oldItem == newItem
        }
    }

    // No-op ListUpdateCallback for differ
    class NoopListUpdateCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}
