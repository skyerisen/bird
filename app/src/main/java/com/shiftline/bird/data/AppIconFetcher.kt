package com.shiftline.bird.data

import android.content.Context
import android.content.pm.PackageManager
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options

/**
 * Custom Coil Fetcher for loading app icons from PackageManager
 */
class AppIconFetcher(
    private val packageName: String,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val drawable = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            context.packageManager.defaultActivityIcon
        }

        return DrawableResult(
            drawable = drawable,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory(private val context: Context) : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return AppIconFetcher(data, context)
        }
    }
}
