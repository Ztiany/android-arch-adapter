plugins {
    alias(libs.plugins.app.common.library)
}

android {
    namespace = "com.android.base.adapter"

    //如果不想生成某个布局的绑定类，可以在根视图添加 tools:viewBindingIgnore="true" 属性。
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // androidx
    api(libs.androidx.annotations)
    api(libs.androidx.recyclerview)
    api(libs.androidx.viewpager2)

    // kotlin
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)

    // adapter
    api(libs.drakeet.multitype)
    api(libs.ztiany.loadmoreadapter)

    // log
    api(libs.jakewharton.timber)
}