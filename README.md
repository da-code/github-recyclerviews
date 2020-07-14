# github-recyclerviews
Android Kotlin project with recyclerviews for Github API's

* Kotlin Flow.
* Navigation Bar.
* ViewModels.
* API call to Github for a list of Google repositories.
* API call to Issues on selected repository.
* Filtering for open or closed issues.
* In memory cache to support local title/description filtering.
* ScrollListener for Github API pagination.
* One fragment with the list of google repositories.
* One fragment with the list of issues for selected repository.


**NOTES:**
* The api limit per ip is about 60 requests per hour.  Scrolling while filtering may hit the api limit quickly since ALL google repositories are being requested by API.  Per page amount is set to 100, but I would reduce this if the per ip limit were higher.


**Future improvements:**
* Pagination library may simplify the ScrollListener logic.
* Room will be added.  With the current in memory cache offering, the submitlist to the adapter doesn't fire the comparater, so it must be overridden, but room should solve this issue.

![Issue List](https://user-images.githubusercontent.com/23284387/87423454-576a8e00-c5a8-11ea-9447-713d4daabaa3.png)
