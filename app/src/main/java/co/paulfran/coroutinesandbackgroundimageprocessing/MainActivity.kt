package co.paulfran.coroutinesandbackgroundimageprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val IMAGE_URL = "https://s3.amazonaws.com/images.seroundtable.com/android-rosie-the-riveter-1527678818.jpg"

    // We need Dispatchers.Main because we cannot update the UI from another thread.
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch Coroutine
        coroutineScope.launch {
            // defer the call to the Dispatchers IO coroutine scope
            val originalDeferred = coroutineScope.async(Dispatchers.IO) { getOriginalBitmap()}

            // await from the image to be processed
            val originalBitmap = originalDeferred.await()

            // to apply the filter requires some processing  which should not be done on the main thread
            val filteredDeferred = coroutineScope.async(Dispatchers.Default) {
                // filter needs to be done after we have received the original bitmap
                applyFilter(originalBitmap)
            }

            // await the filter to be applied to the original image
            val filteredBitmap = filteredDeferred.await()

            // load the filtered image once we have it
            loadImage(filteredBitmap)
        }

    }

    // This is a network call and we cannot do this on the main thread. We therefore need to open up a coroutine scope to make this call
    private fun getOriginalBitmap() =
        // Decode Stream into a Bitmap
        URL(IMAGE_URL).openStream().use {
            BitmapFactory.decodeStream(it)
        }

    private fun applyFilter(originalBitmap: Bitmap) = Filter.apply(originalBitmap)

    private fun loadImage(bmp: Bitmap) {
        // hide progress bar
        progressBar.visibility = View.GONE
        // set the image
        imageView.setImageBitmap(bmp)
        // set the image visibility to visible
        imageView.visibility = View.VISIBLE
    }


}
