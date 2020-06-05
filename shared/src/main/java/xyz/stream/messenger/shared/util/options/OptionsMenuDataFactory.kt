package xyz.stream.messenger.shared.util.options

import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.pojo.OptionsItem
import xyz.stream.messenger.shared.data.pojo.OptionsSection
import xyz.stream.messenger.shared.util.options.OptionsMenuDataFactory.OptionsItemDataFactory.getAccountSection
import xyz.stream.messenger.shared.util.options.OptionsMenuDataFactory.OptionsItemDataFactory.getPrimarySection
import xyz.stream.messenger.shared.util.options.OptionsMenuDataFactory.OptionsItemDataFactory.getSettingsSection

object OptionsMenuDataFactory {
    fun getOptions(): List<OptionsSection> {
        val sections = mutableListOf<OptionsSection>()
        sections.add(OptionsSection(getAccountSection()))
        sections.add(OptionsSection(getPrimarySection()))
        sections.add(OptionsSection(getSettingsSection()))
        return sections
    }

    object OptionsItemDataFactory {
        private val accountSectionLabels = arrayListOf(R.string.menu_account)
        private val accountSectionImages = arrayListOf(R.drawable.ic_devices)
        private val accountSectionIds = arrayListOf(R.id.drawer_about)

        private val primarySectionLabels = arrayListOf(
                R.string.menu_edit_folders_no_ellipse,
                R.string.menu_private_conversations,
                R.string.menu_mute_contacts,
                R.string.menu_invite
        )
        private val primarySectionImages = arrayListOf(
                R.drawable.ic_folder,
                R.drawable.ic_lock,
                R.drawable.ic_block_small,
                R.drawable.ic_invite
        )
        private val primarySectionIds = arrayListOf(
                R.id.drawer_edit_folders,
                R.id.drawer_private,
                R.id.drawer_mute_contacts,
                R.id.drawer_invite
        )

        private val settingsSectionLabels = arrayListOf(
                R.string.menu_settings,
                R.string.menu_help,
                R.string.menu_about
        )
        private val settingsSectionImages = arrayListOf(
                R.drawable.ic_settings,
                R.drawable.ic_help,
                R.drawable.ic_about
        )
        private val settingsSectionIds = arrayListOf(
                R.id.drawer_settings,
                R.id.drawer_help,
                R.id.drawer_about
        )

        fun getAccountSection(): List<OptionsItem> {
            val items = mutableListOf<OptionsItem>()
            for (i in 0 until accountSectionLabels.size) {
                val item = OptionsItem(accountSectionLabels[i], accountSectionImages[i], accountSectionIds[i])
                items.add(item)
            }

            return items
        }

        fun getPrimarySection(): List<OptionsItem> {
            val items = mutableListOf<OptionsItem>()
            for (i in 0 until primarySectionLabels.size) {
                val item = OptionsItem(primarySectionLabels[i], primarySectionImages[i], primarySectionIds[i])
                items.add(item)
            }

            return items
        }

        fun getSettingsSection(): List<OptionsItem> {
            val items = mutableListOf<OptionsItem>()
            for (i in 0 until settingsSectionLabels.size) {
                val item = OptionsItem(settingsSectionLabels[i], settingsSectionImages[i], settingsSectionIds[i])
                items.add(item)
            }

            return items
        }
    }
}