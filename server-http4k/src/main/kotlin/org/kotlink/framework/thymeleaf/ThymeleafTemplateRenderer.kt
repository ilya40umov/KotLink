package org.kotlink.framework.thymeleaf

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.http4k.template.TemplateRenderer
import org.http4k.template.ViewModel
import org.http4k.template.ViewNotFound
import org.kotlink.Constants.IDE_RESOURCES_DIRECTORY
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.TemplateEngine
import org.thymeleaf.cache.StandardCacheManager
import org.thymeleaf.context.Context
import org.thymeleaf.exceptions.TemplateInputException
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.FileNotFoundException

class ThymeleafTemplateRenderer(hotReload: Boolean) : TemplateRenderer {

    private val engine: ITemplateEngine = TemplateEngine().apply {
        addDialect(LayoutDialect())
        engineContextFactory
        if (hotReload) {
            cacheManager = StandardCacheManager().apply {
                templateCacheMaxSize = 0
            }
            setTemplateResolver(fileTemplateResolver())
        } else {
            setTemplateResolver(cachingClasspathResolver())
        }
    }

    override fun invoke(viewModel: ViewModel): String = try {
        engine.process(viewModel.template(), Context().apply {
            setVariable("model", viewModel)
        })
    } catch (e: TemplateInputException) {
        when (e.cause) {
            is FileNotFoundException -> throw ViewNotFound(viewModel)
            else -> throw e
        }
    }

    companion object {
        fun fileTemplateResolver(): ITemplateResolver = FileTemplateResolver().apply {
            prefix = "${IDE_RESOURCES_DIRECTORY}/templates/"
            suffix = ".html"
        }

        fun cachingClasspathResolver(): ITemplateResolver = ClassLoaderTemplateResolver(
            ClassLoader.getSystemClassLoader()
        ).apply {
            prefix = "templates/"
            suffix = ".html"
        }
    }
}