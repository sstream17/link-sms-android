package xyz.stream.messenger.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment

import xyz.stream.messenger.R
import xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper

@Suppress("DEPRECATION")
class OnboardingActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper.tutorialStarted(this)

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.onboarding_title_1), getString(R.string.onboarding_content_1),
                R.drawable.ic_onboarding_inbox,
                resources.getColor(R.color.materialTeal)))

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.onboarding_title_2), getString(R.string.onboarding_content_2),
                R.drawable.ic_onboarding_devices,
                resources.getColor(R.color.materialLightBlue)))

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.onboarding_title_3), getString(R.string.onboarding_content_3),
                R.drawable.ic_onboarding_person,
                resources.getColor(R.color.materialLightGreen)))

        val done = findViewById<View>(R.id.done) as Button
        val skip = findViewById<View>(R.id.skip) as Button

        done.text = getString(R.string.onboarding_done)
        skip.text = getString(R.string.api_skip)

        showSkipButton(true)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()

        xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper.tutorialFinished(this)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()

        xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper.tutorialSkipped(this)
        xyz.stream.messenger.api.implementation.firebase.AnalyticsHelper.tutorialFinished(this)
    }
}
