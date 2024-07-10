plugins {
    alias(libs.plugins.app.common.library)
    alias(libs.plugins.vanniktech.maven.publisher)
}

// https://github.com/drakeet/MultiType

android {
    namespace = "com.android.base.adapter"

    sourceSets {
        getByName("main") {
            java.srcDir("src/github/java")
            res.srcDir("src/github/res")
        }
    }

    //如果不想生成某个布局的绑定类，可以在根视图添加 tools:viewBindingIgnore="true" 属性。
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //  base
    api(libs.base.arch.viewbinding)

    // androidx
    api(libs.androidx.annotations)
    api(libs.androidx.recyclerview)
    api(libs.androidx.viewpager2)
    compileOnly(libs.androidx.paging.runtime)

    // kotlin
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)

    // adapter
    api(libs.ztiany.loadmoreadapter)

    // log
    api(libs.jakewharton.timber)
}