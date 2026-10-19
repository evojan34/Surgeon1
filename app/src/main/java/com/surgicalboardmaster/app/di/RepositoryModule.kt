package com.surgicalboardmaster.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    // تم تعطيل جميع الروابط (Bindings) مؤقتاً كما اتفقنا.
    // واجهاتك وتصميمك (UI) في أمان تام ولن يُمحى منها شيء.
    // الهدف هنا هو إجبار المترجم على تجاوز مرحلة الانهيار (ValidationException).
    // سنعيد تفعيلها لاحقاً بخطوة بسيطة جداً.

}
