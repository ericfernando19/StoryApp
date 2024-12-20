import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.data.UserRepository
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

        // Pastikan data dummy tidak kosong
        assertTrue("Dummy stories should not be empty", dummyStories.isNotEmpty())

        // Create PagingData using dummyStories
        val data = PagingData.from(dummyStories)

        // Create a flow of PagingData
        val expectedStories = flow {
            emit(data)
        }

        // Mock repository method to return the flow of PagingData
        Mockito.`when`(storyRepository.getStoriesPaged(Mockito.anyString()))
            .thenReturn(expectedStories)

        // Setup differ
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = Dispatchers.Main,
        )

        // Launch coroutine to collect data
        val job = launch {
            storyViewModel.getPagedStories().collect { pagingData ->
                differ.submitData(pagingData) // Submit PagingData to differ
            }
        }

        // Simulate the flow to advance until idle
        advanceUntilIdle()

        // Cancel job after test completion
        job.cancel()

        // Log the result to help with debugging
        val dataList = differ.snapshot()
        println("Data in differ: $dataList")

        // Verify data in differ
        assertNotNull(dataList)
        assertTrue("Data list should not be empty", dataList.isNotEmpty()) // Ensure the list is not empty

        // Ensure size matches
        assertEquals("Size of list does not match", dummyStories.size, dataList.size)

        // Ensure the first item is the same
        assertEquals("First item does not match", dummyStories[0], dataList[0])

        // Optionally, you can check the entire list:
        dummyStories.forEachIndexed { index, story ->
            assertEquals("Item at index $index does not match", story, dataList[index])
        }
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.empty() // Simulate empty PagingData
        val expectedStories = flowOf(data)

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
