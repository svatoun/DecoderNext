# DecoderNext

This is an experimental UI for [JMRI DecoderPro](https://www.jmri.org/help/en/html/apps/DecoderPro/index.shtml) based on [NetBeans](http://www.netbeans.org) 
platform.

**At this moment, it is just a dump of local sources, even not compilable on different machine**, treated as a backup. It will eventually improve, if reasonably complete. **The repository is not usable at this moment**.

The original DecoderPro UI is completely diverging from any desktop application guidelines, so it is bloated, over-complex, bad-looking without feedback, validation, proper window and document (roster entry contents) management etc.

I expect this prototype to be similarly bad, since it has to generate UIs the same way as original DecoderPro (I am no UX expert), but will focus on certain user experience aspects:
- all text fields should have a validation feedback.
- all special values should be suggested in a completion-like popup.
- yes/no choices should be presented as checkboxes
- layout of controls should follow common alignment and positioning guidelines
- the mess of many decoder pro tabs should be more organized
- entries from Roster will be treated more like documents; once changed, they will appear in modified documents list etc

Primary usage scenarios for DecoderNext:
- maintenance of decoder settings on the local harddrive, including versioning.
- programming of rolling stock and accessory decoders.
- reading / troubleshooting of decoders.

Portions of the project could be eventually donated back, and are intentionally written as portable as possible, especially some UI controls or snippets. But there's a strong pushback for any ideas or changes since the current UI is seen as optimal by JMRI developer social bubble. There's a strong "not invented here". Good luck.

