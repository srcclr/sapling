# Sapling

![sapling](docs/sapling.png)

[Live demo](https://sapling.netlify.com/)

Create an account or use these credentials:

username: `demo@example.com`<br/>
password: `test`

## Using Sapling during [**PI planning**](https://www.scaledagileframework.com/pi-planning/)

The tool Sapling allows participants to do SAFe PI planning in a remote and distributed fashion in conjunction with video conferencing tools such as Zoom. Here is a sample agenda that explains how to use Sapling and Zoom.

**Prerequisite -**

- A zoom call where the [RTE](https://www.scaledagileframework.com/release-train-engineer-and-solution-train-engineer/) and [Scrum Masters](https://www.scaledagileframework.com/scrum-master/) (SM) are hosts
- Each scrum team has a board created in Sapling
- Product vision, architecture vision is ready to be shared with the teams

**Agenda –**

- Context setting with all members of the [Agile Release Train](https://www.scaledagileframework.com/agile-release-train/) for product and architecture roadmap
- Breakout into scrum teams using [Zoom&#39;s breakout rooms](https://support.zoom.us/hc/en-us/articles/206476313-Managing-Video-Breakout-Rooms) feature
  - [Product Owner](https://www.scaledagileframework.com/product-owner/) shares planning context describing features in priority order – this feeds into the &quot;Epic&quot; list in Sapling
  - Create sprints and fill in the &quot;Capacity&quot; of the team
  - Breakdown the epic by creating &quot;Stories&quot; in the &quot;Backlog&quot; and assigning it to the epic
  - Create story dependencies using the &quot;Dependencies&quot; section in the story.
  - Use the &quot;Auto-arrange&quot; feature to automatically fill the stories created into sprints based on epic priority, story dependencies, story points and capacity
  - &quot;Pin&quot; a story to a particular sprint if you don&#39;t want the auto-arrange feature to move the story
- [Scrum of Scrums](https://www.scaledagileframework.com/program-increment/)
  - While the teams are breaking down features, the SMs can move back to the main meeting to discuss progress and do check-ins
  - RTE being the host can also join the different rooms to observe any discussion if required
  - RTE and SMs can use the &quot;Broadcast message&quot; feature to give instructions or timing announcements.
- &quot;Export to CSV&quot; allows you to move your plans into your Agile Project Management tool such as JIRA.

## Deployment

- [Architecture](docs/architecture.md)
- [Deploying to Heroku](docs/heroku.md)
- [Deploying](docs/frontend.md) the [front end](https://github.com/srcclr/sapling-frontend)

## Development

Starting a local database

```sh
scripts/db.sh fresh
```

Updating jOOQ generated code

```sh
mvn compile -P codegen
```
