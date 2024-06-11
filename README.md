# Introduction
Hi there! Thank you for taking the time to conduct the Weedmaps Android code challenge! A project template has been provided for you. Please focus on meeting the requirements, if you’d like to showcase additional skills you may feel free to do so although it is not expected. Feel free to take any creative liberties with interface design so long as they do not deviate from the requirements of the challenge.

## Expected Time Commitment
We ask candidates to allocate **4-6 hours** for this assignment. Keep in mind this doesn’t have to be all in one sitting. You may spend no longer than **5 days** to complete this assignment, however if more time is needed please reach out!

# Overview
Leveraging the [Yelp Fusion API](https://docs.developer.yelp.com/docs/fusion-intro), build an app that displays businesses based on the user’s input in the search field. Follow the [instructions here](https://docs.developer.yelp.com/docs/fusion-authentication) to obtain an API Key for the Yelp Fusion API (don’t be worried about hard-coding the key into your app for this assignment). You may use the [designs here](https://www.figma.com/file/vcfmVmKtPf4hPwIm12jfQ5/Android-Homework?type=design&node-id=2-9&mode=design&t=EXoeVDU6A6rwhxmO-0) as a guideline for your interface. For your UI, please utilize [Jetpack Compose](https://developer.android.com/jetpack/compose) if possible, and avoid using any 3rd party UI frameworks. If you aren’t familiar with Compose, feel free to write in Android XML instead. Do not feel obligated to utilize all the properties and stubs in the project template.

## Dependencies
Here are a few dependencies we have added to the project. **Feel free to add any other dependencies and/or frameworks that you'd like but we may ask about why you chose to include them.**
- [Kotlin Extensions](https://kotlinlang.org/docs/extensions.html)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [MockK](https://notwoods.github.io/mockk-guidebook/docs/quick/android/) (Unit Testing)
- [Koin](https://insert-koin.io/)
- [Retrofit](https://square.github.io/retrofit/)
- [GSON](https://github.com/google/gson)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Coil for Compose](https://coil-kt.github.io/coil/compose/)

## Requirements
Requirements will differ based off of the level you are applying for. Please consult with your recruiter if you need clarification on requirements.
**Important:** The Yelp Fusion API has a **query-per-second limit of 5**, and limited to **500 API requests per 24 hours.** Keep this in mind when configuring your API requests, otherwise you’ll receive a 429 HTTP error code for some of your requests. If you’d like, show us how you’d mitigate the query-per-second issue in your approach.

## FAQ
- What if a link is broken above? Please reach out and we'll get those fixed.
- What if I have any questions? Pretend your product manager is on vacation, please use your best judgment and be able to speak as to why you made certain decisions.
- Can I modify the designs? Yes, **within reason.** Stay as true to them as you can, and be prepared to explain any changes you make.

# Submission
Clone the git repository and complete using local IDE (Android Studio). Commits and created branches will show up on HackerRank web repository viewer. Please have the final submission merged into the master branch. Submit on HackerRank when the project is completed. If HackerRank isn't being used for this assessment, either ZIP the project file or provide a link to the public repository with your completed project.
**Note:** Disregard the "Run Tests" button on the bottom right of the HackerRank project viewer.
