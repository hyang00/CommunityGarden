APP IDEA 2 - README Template
===

# TBD - (connect neighbors through gardening)

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
This app focuses on building local communities through gardening.

### App Evaluation
- **Category:** Social/Lifestyle
- **Mobile:** 
    - Use camera for profile pic + pics of garden/plants
    - Use location to match people near each other
    - Use maps to help people get to each other's garden
- **Story:** I think that this app would be pretty compelling for a lot of suburban middle aged/older people
    - My friends would likely not use this app and think it's pretty lame unfortunately
- **Market:** Pretty niche market
    - audience: retired/people w/ a lot of time who live in the suburbs and like to garden
- **Habit:** Hopefully going to checkout other people's garden would become a hobby/fun activity so maybe something to do every weekend
- **Scope:** Clearly defined
    - Technically challenging??
    - Stripped down version = just allow people to create profiles + host/attend garden tours

## Product Spec

### 1. User Stories (Required and Optional)
**P0**
* User can create a new account/profile
* User can login/log out 
* User can view garden tours near them
* User can host/post a garden tour
* User can use the camera to take picture for their profile/event


**P1**
* Get user location automatically
* User can rsvp for an event
* User can view who is coming to their event
* User can login/sign up through facebook/google
* Show location on map in app
* User can view/edit their profile
* User can view other people's profile
* User can view list of events they have signed up

**P2**

* User can get reminders about events they've rsvp'd for
* Tour host can set a cap on number of attendees
* User can waitlist themselves on an event and get notified if someone else unregisters
* User can export event to google calendar https://developers.google.com/calendar
* User can filter events by date
* Improve what events show up in a users feed
* User can message event host
* make users use a key to access the database

**Optional Nice-to-have Stories**

* Add tags to events for sorting by analyzing photos?
* User can view/search other users
* If there are a lot of events on the same day in the same area/option to auto link into a walk through neighboorhood type of event?
* Crowd source popular plants that frow well in the area
* Connect gardeners w/ tree companies trying to get rid of wood chips so that they don't go to the landfill https://getchipdrop.com/
* Streamline plant/seed sharing
### 2. Screen Archetypes

* Login Screen
   * user can login
   * user can create a new account
* Create new account
   * Add location (required)
   * Add interests (optional)
   * Add profile pic (optional)
* Event Feed 
   * Can filter/search events
   * Can rsvp to an event
   * Can export event to google calendar
* Host Event Screen 
    * Can use camera to add photos to event
    * Can add a description, time, + location

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* (Home)/Event Feed
* Host Event 
* Profile Page

**Flow Navigation** (Screen to Screen)

* Login Screen
   => Home
* Home/Event Feed
   => detail view of event
* Host Event
    => Back to Home after creating

## Wireframes
[Add picture of your hand sketched wireframes in this section]
![](https://i.imgur.com/x1dCRCw.jpg)

### [BONUS] Digital Wireframes & Mockups
https://www.figma.com/file/jsfcst2Yj7XuoXJfFmYpXf/Garden-Tour-App?node-id=0%3A1

### [BONUS] Interactive Prototype

## Schema 
[This section will be completed in Unit 9]
### Models
#### Users
- authentification (username + password)
#### Profile

| Property | Type | Description |
| -------- | -------- | -------- |
| user  | Pointer to User    | who's profile it is |
| profilePic     | File     | image for user's profile     |
| Bio    | String    | info about user    |
| Location   | String ??    | where the user is located |

#### Post/Event
| Property | Type | Description |
| -------- | -------- | -------- |
| author  | Pointer to User    | who's hosting the event |
|  title   | String    | tour name   |
|  description   | String    | info about tour    |
| picture     | File     | image for tour    |
| time    | Date    | when's the event happening    |
| Location   | String ??    | where the event is happening |
https://firebase.google.com/docs/firestore/manage-data/data-types?hl=el
### Networking
- Login Screen
    - (Read) Get user info/authenticate
- Create new Profile Screen
    - (Create) make new login/user info
    - (Create) make new profile
- Event Feed
    - (Read) Get events near user
- Host Event
    - (Create) make a new Post
- [Create basic snippets for each Parse network request]
- https://trefle.io/reference
- [OPTIONAL: List endpoints if using existing API such as Yelp]
